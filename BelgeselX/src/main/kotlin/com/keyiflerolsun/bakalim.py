# ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

from Kekik.cli import konsol
from requests  import get
from parsel    import Selector
from re        import findall, search

bolum_lik = "https://belgeselx.com/belgesel/uzaylilar-ve-kutsal-mekanlar-antik-uzaylilar"
istek     = get(bolum_lik)

alternatifler = findall(r"""<iframe\s+[^>]*src=\\\"([^\\\"']+)\\\"""", istek.text)
konsol.print(alternatifler)

for alternatif in alternatifler:
    iframe_istek = get(alternatif, headers={"Referer": bolum_lik})
    if "new4.php" in alternatif:
        for kaynak in findall(r"""file:\"([^\"]+)\", label: \"([^\"]+)""", iframe_istek.text):
            konsol.print(f"iframe » {kaynak[0]}\nkalite » {kaynak[1]}\n-------\n")