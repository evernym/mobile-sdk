# 9. Credential with attachments

Credentials with attachments can be issued and attachments can be shown in client application.

Let's consider an example how attachments can be implemented as base64 encoded values.

> **NOTE**: base64 encoded strings is just a representation of attachment, any other ways are valid and up to your system architecture.

Assume that attachments will be implemented the next way:

* Attachments should be added in form of base64 encoded string.
* Photo, audio, video and other common documents can be attached to a credential and supported in client application.
* Credential field name *must* have `_link` postfix to be treated as an attachment by the application.
* All supported types are listed in [Supported mime types](#supported-mime-types) section.

## Issuing credentials with attachments

> **NOTE**: for general credential messages structure see [Credentials](5.Credentials.md) section.

To use attachment field in credential the next JSON string will be used as value in credential attributes list:

```json
{
  "mime-type": "image/png",
  "extension": "png",
  "name": "my_photo.png",
  "data": {
    "base64": "BASE_64_ENCODED_STRING"
  }
}
```

`mime-type`, `extension` and `name` should be filled with file name, its extension and mime type.
Please refer to [Supported mime types](#supported-mime-types) section to find examples of mime types which can be supported in client application.
`data` field should contain JSON object which has `base64` field.
Attached file should be encoded as base64 string and this string should be used and `base64` field's value.

Also to mark field as an attachment, `_link` postfix *must* be added to credential field name.

So credential attributes in `credential_attrs` field should look like this

```json
{
  "First Name": "Amy",
  "Last Name": "Reeders",
  "Photo_link": "{\"mime-type\": \"image/png\", \"extenstion\": \"png\", \"name\": \"my_photo.png\", \"data\": {\"base64\":\"data:image/png;base64....\"}}"
}
```

## Requesting field with attachment in proof request

> **NOTE**: for proof request messages structure see [Proofs](6.Proofs.md) section

To request a credential with an attachment, field name with `_link` postfix should be used in proof request attributes.
So requested attributes will look like this

```json
"requested_attributes": {
  "First Name": {
    "name": "First Name"
  },
  "Last Name": {
    "name": "Last Name"
  },
  "Photo": {
    "name": "Photo_link"
  },
}
```

## Supported mime types

* Photo types:
  * `image/jpeg`
  * `image/png`
  * `image/jpg`
* MS Word types:
  * `application/msword`
* MS Excel types:
  * `application/vnd.ms-excel`
* MS CSV types:
  * `text/csv`
* MS Powerpoint types:
  * `application/vnd.ms-powerpoint`  
* PDF types:
  * `application/pdf`
* Audio and video types:
  * `audio/mp4`
  * `audio/mpeg`
  * `audio/mp3`
  * `video/mp4`

## Limitation

There is a limitation in ~400KB of the maximum allowed size for Credential Values that can be processed.
