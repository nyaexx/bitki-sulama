# Akıllı Bitki Sulama
<p align="center">
  <img src="https://github.com/nyaexx/bitki-sulama/blob/main/.github/bitkisulamalogo.png" width="150px">
</p>


<p align="center">
  <a href="https://github.com/nyaexx/bitki-sulama/releases">
    <img src="https://img.shields.io/github/downloads/nyaexx/bitki-sulama/total.svg?label=Toplam%20indirmeler">
  </a>
</p>

---
Bu proje, belirli bir bitkinin toprak nemi ve hava değerlerini ölçerek bir Arduino devresiyle otomatik sulama işlemleri yapmayı sağlar. Ayrıca, verileri gözlemleyebileceğiniz ve Bluetooth aracılığıyla bu verileri çekebileceğiniz bir Android uygulaması da bulunmaktadır.

---
### Android Uygulamasını İndirme:

Uygulamanın Son Sürümünü buradan indirebilirsiniz:

[![](https://img.shields.io/badge/Bitki%20Sulama-n1.2-blue)](https://github.com/nyaexx/bitki-sulama/releases/tag/n1.2)

### Android Uygulamasının Genel Özellikleri:
- Bluetooh ile cihazdan verilerin okunup ekrana yazılması.
- Manuel olarak su motorunun çalıştırılması

---

### Android Uygulaması Üzerinde Değişiklik Yapma:

**Gereklilikler:**

    Android Studio (Arctic Fox veya üzeri)

    Kotlin

    Gradle (Android Studio ile birlikte gelir)

    Min SDK: 24, Target SDK: 35

    Fiziksel Android cihaz (Bluetooth bağlantısı için)

**Nasıl Çalıştırılır?**

- Repo'yu klonla:

  -  ``git clone https://github.com/nyaexx/bitki-sulama/``

- Android Studio ile aç.

- Gradle sync tamamlandıktan sonra fiziksel cihaza deploy et.

- Uygulama açıldığında önce Bluetooth cihazına bağlan, ardından veri izleme ekranına geçiş yapılır.

---

### Devre Kurulumu ve Kullanımı
Kurulum için aşağıdaki bağlantı şemasına göre devreyi kurun:
<p align="center">
  <img src="https://github.com/nyaexx/bitki-sulama/blob/main/Arduino%20Kodlar%C4%B1/ba%C4%9Flanti%C5%9Femas%C4%B1.png" width="">
</p>

> [!NOTE]
> Kullanım içinse [bu dizindeki](https://github.com/nyaexx/bitki-sulama/tree/main/Arduino%20Kodlar%C4%B1) arduino kodlarını ve libraries klasöründeki kütüphaneleri cihazınıza yükleyip kullanabilirsiniz.
>
> Unutmayın eğer orjinal arduino uno kullanmıyorsanız kesinlikle [Arduino Kodları](https://github.com/nyaexx/bitki-sulama/tree/main/Arduino%20Kodlar%C4%B1) dinizindeki libraries klasöründe bulunan CH340 kütüphanesinin kurulumunu yapınız. Aksi takdirde kurulumunu yapmanıza gerek yoktur.

> [!CAUTION]
> ``Bluetooht Bağlantısı İçin Yapılması Gerekenler:``
**İlk bağlantıda telefonun bluetooth bağlantı menüsü açılıp "HC-06" seçilip şifre 1234 girilmelidir.
Uygulama içinden bağlantıda bağlantı sorunu yaşanırsa uno kart üzerinde bulunan kırmızı reset düğmesine basıldıktan hemen sonra eşleşme yapılmalıdır.
Bluetooth modül üzerinde bulunan kırmızı led yanıp söndüğünde "cihaz aranıyor" sürekli yandığında "bağlantı sağlandı" manasına gelir.
Eğer bluetooth bağlı gözükürken eşleşme yapılamazsa devrede elektriği kesin ışıklar sönene kadar bekleyin ve tekrar elektriği bağlayın sorun düzelecektir.**



---

> [!NOTE]
> Yardım veya herhangi bir soru için discord: nyaexx
> 
> Repoya bir şey eklemek ister veya bir hatayı düzeltmek isterseniz pull request atabilirsiniz.

