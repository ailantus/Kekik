# ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

from httpx  import Client as Session
from parsel import Selector
from re     import findall
import base64, json

def atob(s:str) -> str:
    return base64.b64decode(s).decode("utf-8")

def rtt(s:str) -> str:
    def rot13_char(c):
        if "a" <= c <= "z":
            return chr((ord(c) - ord("a") + 13) % 26 + ord("a"))
        elif "A" <= c <= "Z":
            return chr((ord(c) - ord("A") + 13) % 26 + ord("A"))
        return c

    return "".join(rot13_char(c) for c in s)

def scx_decode(scx:dict) -> dict:
    for key, item in scx.items():
        item["tt"] = atob(item["tt"])
        if "t" in item["sx"]:
            item["sx"]["t"] = [atob(rtt(ii)) for ii in item["sx"]["t"]]
        if "p" in item["sx"]:
            item["sx"]["p"] = [atob(rtt(ii)) for ii in item["sx"]["p"]]
        scx[key] = item

    return scx

def rapid2m3u8(url:str) -> str:
    oturum = Session()
    oturum.headers.update({"User-Agent":"Mozilla/5.0"})

    istek       = oturum.get(url)
    escaped_hex = findall(r'file": "(.*)",', istek.text)[0]

    return bytes.fromhex(escaped_hex.replace("\\x", "")).decode("utf-8")

def fullhdfilmizlesene(url:str) -> str:
    oturum = Session()
    oturum.headers.update({"User-Agent":"Mozilla/5.0"})

    istek  = oturum.get("https://www.fullhdfilmizlesene.pw/film/hizli-ve-ofkeli-10-fast-x-fhd4/")
    secici = Selector(istek.text)

    script = secici.xpath("(//script)[1]").get()

    scx_data = json.loads(findall(r'scx = (.*?);', script)[0])
    scx      = scx_decode(scx_data)

    rapidvid = scx["atom"]["sx"]["t"][0]

    return rapid2m3u8(rapidvid)

print(fullhdfilmizlesene("https://www.fullhdfilmizlesene.pw/film/hizli-ve-ofkeli-10-fast-x-fhd4/"))