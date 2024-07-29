from json                import loads
from Crypto.Hash         import MD5
from Crypto.Cipher       import AES
from Crypto.Util.Padding import unpad
from base64              import b64decode

def decrypt_aes_with_custom_kdf(crypted_data, password) -> str:
    """
    AES/CBC/PKCS5Padding şifreleme şemasını kullanarak şifre çözme işlemi yapar.

    :param crypted_data: JSON formatında şifrelenmiş veri (ct, iv, s içerir).
    :param password: Anahtar türetmede kullanılacak şifre.
    :return: Çözülmüş veri (string olarak).
    """

    def generate_key_and_iv(password, salt, key_length=32, iv_length=16, iterations=1):
        """Anahtar ve IV oluşturmak için bir KDF fonksiyonu."""
        d = d_i = b""
        while len(d) < key_length + iv_length:
            d_i = MD5.new(d_i + password + salt).digest()
            for _ in range(1, iterations):
                d_i = MD5.new(d_i).digest()
            d += d_i
        return d[:key_length], d[key_length : key_length + iv_length]

    def hex_to_bytes(hex_str):
        """Hex string'i byte array'e çevirir."""
        return bytes.fromhex(hex_str)

    data      = loads(crypted_data)

    key, iv   = generate_key_and_iv(password, hex_to_bytes(data["s"]), iv_length=len(data["iv"]) // 2)

    cipher    = AES.new(key, AES.MODE_CBC, iv)
    decrypted = unpad(cipher.decrypt(b64decode(data["ct"])), AES.block_size)

    return decrypted.decode("utf-8")

__token = "710^8A@3@>T2}#zN5xK?kR7KNKb@-A!LzYL5~M1qU0UfdWsZoBm4UUat%}ueUv6E--*hDPPbH7K2bp9^3o41hw,khL:}Kx8080@M"
__data  = """{"ct":"DFZbtroIV0JzoVqXJK6z9YsYmXbQSy9i4xAyapcyZW4aEN8ttpZ4qtvPSGGygN/y0DmxFUn13n0EflOP+cS8XTFPs7clwvTKASigkWmrHucq4mV3Ijze2W5WJe4W6lyjUjBBqSTA96pO5y7+gC4Z5yxFJBGK0YfcHl5B0H4VbvXm0ki4wMYkieeNXCfKUnm3","iv":"2964fbb0f31c3d36e4e99fa82655456b","s":"41b10ce459db9da2"}"""
encrypted = decrypt_aes_with_custom_kdf(__data, __token.encode()).replace("\\", "")
print(encrypted)