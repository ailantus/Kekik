# ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

from Kekik.cli import konsol
from httpx     import Client as Session
from parsel    import Selector
import re, base64, json

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

def unpack_packer(source: str) -> str:
    """https://github.com/beautifier/js-beautify/blob/main/python/jsbeautifier/unpackers/packer.py"""

    def clean_escape_sequences(source: str) -> str:
        source = re.sub(r'\\\\', r'\\', source)
        source = source.replace("\\'", "'")
        source = source.replace('\\"', '"')
        return source

    source = clean_escape_sequences(source)

    def extract_arguments(source: str) -> tuple[str, list[str], int, int]:
        match = re.search(r"}\('(.*)',(\d+),(\d+),'(.*)'\.split\('\|'\)", source, re.DOTALL)

        if not match:
            raise ValueError("Invalid P.A.C.K.E.R. source format.")

        payload, radix, count, symtab = match.groups()

        return payload, symtab.split("|"), int(radix), int(count)

    def convert_base(s: str, base: int) -> int:
        alphabet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

        return sum(alphabet.index(char) * (base**idx) for idx, char in enumerate(reversed(s)))

    payload, symtab, radix, count = extract_arguments(source)

    if count != len(symtab):
        raise ValueError("Malformed P.A.C.K.E.R. symtab.")

    def lookup_symbol(match: re.Match) -> str:
        word = match[0]

        return symtab[convert_base(word, radix)] or word

    unpacked_source = re.sub(r"\b\w+\b", lookup_symbol, payload)

    return unpacked_source

def rapid2m3u8(url:str) -> str:
    oturum = Session()
    oturum.headers.update({"User-Agent":"Mozilla/5.0"})

    istek = oturum.get(url)
    try:
        escaped_hex = re.findall(r'file": "(.*)",', istek.text)[0]
    except Exception:
        eval_jwsetup = re.compile(r'\};\s*(eval\(function[\s\S]*?)var played = \d+;').findall(istek.text)[0]
        jwsetup      = unpack_packer(unpack_packer(eval_jwsetup))
        escaped_hex  = re.findall(r'file":"(.*)","label', jwsetup)[0]

    return bytes.fromhex(escaped_hex.replace("\\x", "")).decode("utf-8")

def trstx2m3u8(url:str) -> list[dict]:
    oturum = Session()
    oturum.headers.update({"User-Agent":"Mozilla/5.0", "Referer":"https://www.fullhdfilmizlesene.de/"})

    istek     = oturum.get(url)
    file      = re.findall(r"file\":\"([^\"]+)", istek.text)[0]
    post_link = file.replace("\\", "")

    post_istek = oturum.post(f"https://trstx.org/{post_link}").json()

    veriler = []
    for bak in post_istek[1:]:
        vid_url = "https://trstx.org/playlist/" + bak.get("file")[1:] + ".txt"
        veriler.append({bak.get("title") : oturum.post(vid_url).text})

    return veriler

def sobreatsesuyp2m3u8(url:str) -> list[dict]:
    oturum = Session()
    oturum.headers.update({"User-Agent":"Mozilla/5.0", "Referer":"https://www.fullhdfilmizlesene.de/"})

    istek     = oturum.get(url)
    file      = re.findall(r"file\":\"([^\"]+)", istek.text)[0]
    post_link = file.replace("\\", "")

    post_istek = oturum.post(f"https://sobreatsesuyp.com/{post_link}").json()

    veriler = []
    for bak in post_istek[1:]:
        vid_url = "https://sobreatsesuyp.com/playlist/" + bak.get("file")[1:] + ".txt"
        veriler.append({bak.get("title") : oturum.post(vid_url).text})

    return veriler

def turboimgz2m3u8(url:str) -> str:
    oturum = Session()
    oturum.headers.update({"User-Agent":"Mozilla/5.0"})

    istek     = oturum.get(url)
    video_url = re.findall(r'file: "(.*)",', istek.text)[0]

    return video_url

def fullhdfilmizlesene(url:str) -> list:
    oturum = Session()
    oturum.headers.update({"User-Agent":"Mozilla/5.0"})

    konsol.print(f"\n\n{url}")
    istek  = oturum.get(url, follow_redirects=True)
    secici = Selector(istek.text)

    script   = secici.xpath("(//script)[1]").get()
    scx_data = json.loads(re.findall(r'scx = (.*?);', script)[0])
    scx_keys = list(scx_data.keys())

    link_list = []
    for key in scx_keys:
        t = scx_data[key]["sx"]["t"]
        if isinstance(t, list):
            link_list.append({key: atob(rtt(elem)) for elem in t})
        if isinstance(t, dict):
            link_list.append({k: atob(rtt(v)) for k, v in t.items()})

    vid_links = []
    for elem in link_list:
        for key, value in elem.items():
            if "rapidvid" in value:
                vid_links.append({key: rapid2m3u8(value)})
                continue

            if "trstx.org" in value:
                vid_links.append({key: trstx2m3u8(value)})
                continue

            if "sobreatsesuyp.com" in value:
                vid_links.append({key: sobreatsesuyp2m3u8(value)})
                continue

            if "turbo.imgz.me" in value:
                vid_links.append({key: turboimgz2m3u8(value)})
                continue

            if "vidmoxy.com" in value:
                vid_links.append({key: rapid2m3u8(value)})
                continue

            vid_links.extend(
                {key: value}
                for bidi in ("proton", "fast", "tr", "en")
                    if bidi in key
            )


    return vid_links

konsol.print(fullhdfilmizlesene("https://www.fullhdfilmizlesene.de/film/makine-2/"))
konsol.print(fullhdfilmizlesene("https://www.fullhdfilmizlesene.de/film/iskence-okulu/"))
konsol.print(fullhdfilmizlesene("https://www.fullhdfilmizlesene.de/film/yedi-yasam/"))
konsol.print(fullhdfilmizlesene("https://www.fullhdfilmizlesene.de/film/cilgin-cocuklar-oyun-bitti-izle-1/"))
konsol.print(fullhdfilmizlesene("https://www.fullhdfilmizlesene.de/film/suclu-den-skyldige/"))
konsol.print(fullhdfilmizlesene("https://www.fullhdfilmizlesene.de/film/vahsiler-hostiles/"))
konsol.print(fullhdfilmizlesene("https://www.fullhdfilmizlesene.de/film/satranc-oyuncusu/"))