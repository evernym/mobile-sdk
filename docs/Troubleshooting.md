## Troubleshooting

This document contains advice on how to troubleshoot the occurred errors. 

#### Catch SDK errors in the code

* Objective-C: `NSError` object extended to contain additional SDK error details within `userInfo` field:
    ```
    {
        "sdk_message": string,
        "sdk_full_message": string,
        "sdk_cause": string,
        "sdk_backtrace": string,
    }
    ```

* Java: `VcxException` class contains additional SDK error details:
    ```
    {
        "sdkErrorCode": int,
        "sdkMessage": string,
        "sdkFullMessage": string,
        "sdkCause": string,
        "sdkBacktrace": string,
    }
    ```

#### Investigate SDK logs

1. Configure SDK [logging](3.Initialization.md#logging) in your application. After doing that you should be able to see log records from SDK.
 
1. Find occurred error details in the log. It should contain either `VcxError` or `IndyError` substrings. 

1. Look for more information about occurred error in [Errors](./Errors.md) and [FAQ](./FAQ.md) documents.

1. If you are not able to solve the error by yourself contact Evernym at [support@evernym.com](mailto:support@evernym.com). Please attach the log file to your message.
