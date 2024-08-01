# ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

from Kekik.cli    import konsol
from cloudscraper import CloudScraper
from parsel       import Selector
from re           import search

oturum = CloudScraper()
istek  = oturum.get("https://www.dizibox.de/hello-tomorrow-1-sezon-1-bolum-izle/")
secici = Selector(istek.text)

iframe = secici.css("div#video-area iframe::attr(src)").get()
iframe = iframe.replace("king.php?v=", "king.php?wmode=opaque&v=")

# oturum.cookies.update({
#     "LockUser"      : "true",
#     "isTrustedUser" : "true",
#     "dbxu"          : "1722403730363"
# })
oturum.headers.update({"Referer": "https://www.dizibox.de/hello-tomorrow-1-sezon-1-bolum-izle/"})
istek  = oturum.get(iframe)
secici = Selector(istek.text)
iframe = secici.css("div#Player iframe::attr(src)").get()

oturum.headers.update({"Referer": "https://www.dizibox.de/"})
istek     = oturum.get(iframe)
cryptData = search(r"CryptoJS\.AES\.decrypt\(\"(.*)\",\"", istek.text).group(1)
cryptPass = search(r"\",\"(.*)\"\);", istek.text).group(1)

from Crypto.Cipher import AES
import hashlib
import base64

class CryptoJS:
    KEY_SIZE    = 32
    IV_SIZE     = 16
    HASH_CIPHER = "AES/CBC/PKCS7Padding"
    AES_MODE    = AES.MODE_CBC
    KDF_DIGEST  = "md5"
    APPEND      = b"Salted__"

    @staticmethod
    def evp_kdf(password, salt, key_size=32, iv_size=16, iterations=1, hash_algorithm="md5"):
        target_key_size = key_size + iv_size
        derived_bytes   = b""
        block           = None

        while len(derived_bytes) < target_key_size:
            hasher = hashlib.new(hash_algorithm)
            if block:
                hasher.update(block)
    
            hasher.update(password)
            hasher.update(salt)
            block = hasher.digest()

            for _ in range(1, iterations):
                block = hashlib.new(hash_algorithm, block).digest()
    
            derived_bytes += block

        return derived_bytes[:key_size], derived_bytes[key_size:key_size + iv_size]

    @staticmethod
    def decrypt(password, cipher_text):
        ct_bytes          = base64.b64decode(cipher_text)
        salt              = ct_bytes[8:16]
        cipher_text_bytes = ct_bytes[16:]

        key, iv = CryptoJS.evp_kdf(password.encode("utf-8"), salt, key_size=CryptoJS.KEY_SIZE, iv_size=CryptoJS.IV_SIZE)

        cipher     = AES.new(key, CryptoJS.AES_MODE, iv)
        plain_text = cipher.decrypt(cipher_text_bytes)

        return CryptoJS._unpad(plain_text).decode("utf-8")

    @staticmethod
    def _unpad(s):
        return s[:-ord(s[-1:])]


decrypted = CryptoJS.decrypt(cryptPass, cryptData)
konsol.print(search(r"file: \'(.*)',", decrypted).group(1))