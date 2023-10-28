# ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

from Kekik.cli    import konsol
from cloudscraper import CloudScraper
from parsel       import Selector
from re           import findall

oturum = CloudScraper()
istek  = oturum.get("https://www.dizimom.pro/kurulus-osman-2-bolum-izle/")
secici = Selector(istek.text)
konsol.print(secici.css("div#vast iframe::attr(src)").get())

oturum.cookies.update({"wordpress_logged_in_94427965a200eb7dd292509ed2c7c018": "keyiflerolsun|1699733740|0JIZp47atdM5omDeHKtXggF9zuccfZvzTDiAowU7lmA|02b7bca0910b49d5d3d44ed24c2cf7181ecbd006ce747ddd7b824f07343a305f"})
istek  = oturum.get("https://www.dizimom.pro/kurulus-osman-2-bolum-izle/")
secici = Selector(istek.text)
konsol.print(secici.css("div#vast iframe::attr(src)").get())