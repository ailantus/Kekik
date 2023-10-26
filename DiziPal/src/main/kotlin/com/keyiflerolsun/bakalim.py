# ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

from Kekik.cli    import konsol
from cloudscraper import CloudScraper
from parsel       import Selector

mainUrl = "https://dizipal639.com"
oturum  = CloudScraper()
istek   = oturum.get(f"{mainUrl}/diziler?kelime=&durum=&tur=1&type=&siralama=")
secici  = Selector(istek.text)

for icerik in secici.css("article.type2 ul li"):
    konsol.print(icerik.css("span.title::text").get())
    konsol.print(icerik.css("a::attr(href)").get())
    konsol.print(icerik.css("img::attr(src)").get())