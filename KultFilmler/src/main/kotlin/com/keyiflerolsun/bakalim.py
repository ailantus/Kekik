# ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

from Kekik.cli    import konsol
from cloudscraper import CloudScraper
from parsel       import Selector
from re           import findall
from base64       import b64decode

oturum  = CloudScraper()
oturum.headers.update({"User-Agent":"Mozilla/5.0", "Referer":"https://kultfilmler.com/"})

film_link = "https://kultfilmler.com/perfume-the-story-of-a-murderer-koku-bir-katilin-hikayesi/2/"

istek = oturum.get(film_link)

def get_iframe(source_code):
    atob = findall(r"""PHA\+[0-9a-zA-Z+\/=]*""", source_code)[0]
    if padding_needed := len(atob) % 4:
        atob += "=" * (4 - padding_needed)

    iframe = b64decode(atob).decode("utf-8")
    return Selector(iframe).css("iframe::attr(src)").get()

konsol.print(get_iframe(istek.text))