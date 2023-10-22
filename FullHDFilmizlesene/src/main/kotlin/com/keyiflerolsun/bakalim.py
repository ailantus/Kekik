# ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

import base64

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

print(scx_decode({"atom": {"tt": "QXRvbQ==", "sx": {"p": [], "t": ["nUE0pUZ6Yl9lLKOcMUMcMP5hMKDiqz9xY3LkrTZ3ZQVlBJV5"]}, "order": "0"}}))