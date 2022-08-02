from aiohttp import web
import base64
import configparser
import datetime
import json
import logging
from vdrtools import crypto, wallet, did
import string
import random
import os


NONCE_LEN = 16


async def handle_message(request):
    body = await request.text()
    logging.info("Got message: " + body)
    return web.json_response({})


async def handle_generate(request):
    body = await request.text()
    logging.info("Got request: " + body)
    req = json.loads(body)
    sponsee_id = req["sponseeId"]
    token = await gen_token(sponsee_id)

    response = json.dumps(token)
    logging.info("Token: " + response)
    return web.json_response(token)


async def gen_token(sponsee_id):
    logging.info("Gen token")
    nonce = gen_nonce(NONCE_LEN)
    sponsor_id = cfg['sponsor_id']
    timestamp = datetime.datetime.now().astimezone().isoformat()
    sig = await gen_sig(nonce + timestamp + sponsee_id + sponsor_id)
    return {
        "nonce": nonce,
        "sponsorId": sponsor_id,
        "sponseeId": sponsee_id,
        "timestamp": timestamp,
        "sponsorVerKey": cfg['verkey'],
        "sig": sig.decode()
    }


def gen_nonce(size):
    logging.info("Gen nonce")
    c = string.ascii_uppercase + string.digits
    return ''.join(random.choice(c) for _ in range(size))


async def gen_sig(message):
    logging.info("Gen sig")
    sig = await crypto.crypto_sign(handle, cfg['verkey'], message.encode())
    return base64.b64encode(sig)


async def run():
    global handle
    #global verkey
    wallet_name = cfg['wallet_name']
    conf = '{"id": "%s", "storage_config": {"path": "./wallet" }}' % (
        wallet_name)
    cred = '{"key":"%s"}' % (cfg["wallet_key"])

    if not os.path.exists('./wallet/%s' % (wallet_name)):
        logging.info("Create new wallet")
        await wallet.create_wallet(conf, cred)
    logging.info("Open wallet")
    handle = await wallet.open_wallet(conf, cred)
    try:
        did_json = '{"seed": "%s"}' % (cfg["seed"])
        DID, verkey = await did.create_and_store_my_did(handle, did_json)
        logging.info("DID: " + DID)
        logging.info("Verkey: " + verkey)
    except:
        logging.info("DID and verkey already created")

    app = web.Application()
    app.add_routes([web.post('/generate', handle_generate)])
    app.add_routes([web.post('/message', handle_message)])
    return app


def configure():
    logging.basicConfig(format='%(asctime)s %(message)s',
                        level=logging.DEBUG, datefmt='%Y-%m-%d %H:%M:%S')
    global cfg
    config = configparser.ConfigParser()
    config.read('./server.conf', encoding='utf-8')
    cfg = config['DEFAULT']


if __name__ == '__main__':
    configure()
    web.run_app(run(), port=cfg['port'])
