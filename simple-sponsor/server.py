from aiohttp import web
import base64
import configparser
import datetime
import json
from indy import crypto, wallet, did
import string
import random
import os

async def handle_post(request):
    body = await request.text()
    print("Got request: " + body)
    req = json.loads(body)
    sponsee_id = req["sponseeId"]
    token = await gen_token(sponsee_id)

    response = json.dumps(token)
    print("Token: " + response)
    return web.json_response(token)

async def gen_token(sponsee_id):
    print("Gen token")
    nonce_len = int(cfg['nonce_len'])
    nonce = gen_nonce(nonce_len)    
    sponsor_id = cfg['sponsor_id']
    timestamp = datetime.datetime.now().astimezone().isoformat()
    sig = await gen_sig(nonce + timestamp + sponsee_id + sponsor_id)
    return {
        "nonce": nonce,
        "sponsorId": sponsor_id,
        "sponsee_id": sponsee_id,
        "timestamp": timestamp,
        "sponsorVerKey": verkey,
        "sig": sig.decode()
    }

def gen_nonce(size):
    print("Gen nonce")
    c=string.ascii_uppercase + string.digits
    return ''.join(random.choice(c) for _ in range(size))

async def gen_sig(message):
    print("Gen sig")
    sig = await crypto.crypto_sign(handle, verkey, message.encode())
    return base64.b64encode(sig)

async def run():
    global handle
    global verkey
    wallet_name = cfg['wallet_name']
    conf ='{"id": "%s", "storage_config": {"path": "./wallet" }}'%(wallet_name)
    cred ='{"key":"%s"}'%(cfg["key"])
    did_json = '{"seed": "%s"}'%(cfg["seed"])
    if not os.path.exists('./wallet/%s'%(wallet_name)):
        print("Create new wallet")
        await wallet.create_wallet(conf, cred)
    print("Open wallet")
    handle = await wallet.open_wallet(conf, cred)
    _, verkey = await did.create_and_store_my_did(handle, did_json)

    app = web.Application()
    app.add_routes([web.post('/', handle_post)])
    return app

def configure():
    global cfg
    config = configparser.ConfigParser()
    config.read('./server.conf', encoding='utf-8')
    cfg = config['DEFAULT']

if __name__ == '__main__':
    configure()
    web.run_app(run(), port=cfg['port'])
