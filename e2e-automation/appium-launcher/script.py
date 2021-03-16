import boto3
import os
import requests
import string
import random
import time
import datetime
import time
import json
import argparse

parser = argparse.ArgumentParser("This is a parser of arguments for appium test launcher")
parser.add_argument("--app_file_path", required=True, dest="appfilepath", help="APK path")
parser.add_argument("--test_file_path", required=True, dest="testfilepath", help="ZIP archive with tests path")
parser.add_argument("--device_type", required=True, dest="device_type", help="Device to test. One of: awsiOS/awsAndroid")
parser.add_argument("--device", required=True, dest="device", help="Device to test. One of: android_top5/galaxyS10/ios_top5/iphone11")
args = parser.parse_args()

project_arn = "arn:aws:devicefarm:us-west-2:111122223333:project:1486a7cc-dd83-4e34-817b-2a0b1320b031"
name_prefix = "MyAppTest"

settings = {
  "awsiOS": {
    "upload_type": "IOS_APP",
    "testSpec": "arn:aws:devicefarm:us-west-2:962610246670:upload:1486a7cc-dd83-4e34-817b-2a0b1320b031/e6e9524b-5ed8-4134-8aa2-0cb4d09418b7",
    "pool": {
      "ios_top5": "arn:aws:devicefarm:us-west-2::devicepool:082d10e5-d7d7-48a5-ba5c-b33d66efa1f5",
      "iphone11": "arn:aws:devicefarm:us-west-2:962610246670:devicepool:1486a7cc-dd83-4e34-817b-2a0b1320b031/9b5597ea-49c4-4c93-937b-f8b2ceb8eb86",
    }
  },
  "awsAndroid": {
    "upload_type": "ANDROID_APP",
    "testSpec": "arn:aws:devicefarm:us-west-2:962610246670:upload:1486a7cc-dd83-4e34-817b-2a0b1320b031/6375220e-6a64-44ee-9ff2-0a6889e3a7a8",
    "pool": {
      "android_top5": "arn:aws:devicefarm:us-west-2::devicepool:082d10e5-d7d7-48a5-ba5c-b33d66efa1f5",
      "galaxyS10": "arn:aws:devicefarm:us-west-2:962610246670:devicepool:1486a7cc-dd83-4e34-817b-2a0b1320b031/4eff1b10-85b6-45b5-b82d-021653dc5df0",
    }
  },
}

# The following script runs a test through Device Farm
#
# Things you have to change:
config = {
    # This is our app under test.
    "appFilePath": args.appfilepath,
    "projectArn": project_arn,
    "testSpecArn": settings[args.device_type]["testSpec"],
    "poolArn": settings[args.device_type]["pool"][args.device],
    "namePrefix": name_prefix,
    # This is our test package. This tutorial won't go into how to make these.
    "testPackage": args.testfilepath
}

client = boto3.client('devicefarm')

unique = config['namePrefix']+"-"+(datetime.date.today().isoformat())+(''.join(random.sample(string.ascii_letters,8)))

print(f"The unique identifier for this run is going to be {unique} -- all uploads will be prefixed with this.")

def upload_df_file(filename, type_, mime='application/octet-stream'):
    response = client.create_upload(projectArn=config['projectArn'],
        name = (unique)+"_"+os.path.basename(filename),
        type=type_,
        contentType=mime
        )
    # Get the upload ARN, which we'll return later.
    upload_arn = response['upload']['arn']
    # We're going to extract the URL of the upload and use Requests to upload it
    upload_url = response['upload']['url']
    with open(filename, 'rb') as file_stream:
        print(f"Uploading {filename} to Device Farm as {response['upload']['name']}... ",end='')
        put_req = requests.put(upload_url, data=file_stream, headers={"content-type":mime})
        print(' done')
        if not put_req.ok:
            raise Exception("Couldn't upload, requests said we're not ok. Requests says: "+put_req.reason)
    started = datetime.datetime.now()
    while True:
        print(f"Upload of {filename} in state {response['upload']['status']} after "+str(datetime.datetime.now() - started))
        if response['upload']['status'] == 'FAILED':
            raise Exception("The upload failed processing. DeviceFarm says reason is: \n"+response['upload']['message'])
        if response['upload']['status'] == 'SUCCEEDED':
            break
        time.sleep(5)
        response = client.get_upload(arn=upload_arn)
    print("")
    return upload_arn


our_upload_arn = upload_df_file(config['appFilePath'], settings[args.device_type]["upload_type"])
our_test_package_arn = upload_df_file(config['testPackage'], 'APPIUM_JAVA_TESTNG_TEST_PACKAGE')
print(our_upload_arn, our_test_package_arn)
# Now that we have those out of the way, we can start the test run...
response = client.schedule_run(
    projectArn = config["projectArn"],
    appArn = our_upload_arn,
    devicePoolArn = config["poolArn"],
    name=unique,
    test = {
        "type":"APPIUM_JAVA_TESTNG",
        "testSpecArn": config["testSpecArn"],
        "testPackageArn": our_test_package_arn
        }
    )
run_arn = response['run']['arn']
start_time = datetime.datetime.now()
print(f"Run {unique} is scheduled as arn {run_arn} ")

try:
    while True:
        response = client.get_run(arn=run_arn)
        state = response['run']['status']
        result = response['run']['result']
        if state == 'COMPLETED' or state == 'ERRORED':
            print(f"Finished with status {state}")
            if result == 'FAILED' or result == 'ERRORED':
                print(f"Finished with result {result}")
                raise Exception('Some tests failed!')
            break
        else:
            print(f" Run {unique} in state {state}, total time "+str(datetime.datetime.now()-start_time))
            time.sleep(10)
except:
    # If something goes wrong in this process, we stop the run and exit.

    client.stop_run(arn=run_arn)
    raise Exception('Something went wrong!')
print(f"Tests finished in state {state} after "+str(datetime.datetime.now() - start_time))
# # now, we pull all the logs.
# jobs_response = client.list_jobs(arn=run_arn)
# # Save the output somewhere. We're using the unique value, but you could use something else
# save_path = os.path.join(os.getcwd(), unique)
# os.mkdir(save_path)
# # Save the last run information
# for job in jobs_response['jobs'] :
#     # Make a directory for our information
#     job_name = job['name']
#     os.makedirs(os.path.join(save_path, job_name), exist_ok=True)
#     # Get each suite within the job
#     suites = client.list_suites(arn=job['arn'])['suites']
#     for suite in suites:
#         for test in client.list_tests(arn=suite['arn'])['tests']:
#             # Get the artifacts
#             for artifact_type in ['FILE','SCREENSHOT','LOG']:
#                 artifacts = client.list_artifacts(
#                     type=artifact_type,
#                     arn = test['arn']
#                 )['artifacts']
#                 for artifact in artifacts:
#                     # We replace : because it has a special meaning in Windows & macos
#                     path_to = os.path.join(save_path, job_name, suite['name'], test['name'].replace(':','_') )
#                     os.makedirs(path_to, exist_ok=True)
#                     filename = artifact['type']+"_"+artifact['name']+"."+artifact['extension']
#                     artifact_save_path = os.path.join(path_to, filename)
#                     print("Downloading "+artifact_save_path)
#                     with open(artifact_save_path, 'wb') as fn, requests.get(artifact['url'],allow_redirects=True) as request:
#                         fn.write(request.content)
                    #/for artifact in artifacts
                #/for artifact type in []
            #/ for test in ()[]
        #/ for suite in suites
    #/ for job in _[]
# done
print("Finished")
