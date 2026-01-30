# Akıllı Bitki Sulama
<p align="center">
  <img src="https://github.com/nyaexx/bitki-sulama/blob/main/.github/biktisulamayenilogo.png" width="150px">
</p>


---
Bu proje, belirli bir bitkinin toprak nemi ve hava değerlerini ölçerek bir Arduino devresiyle otomatik sulama işlemleri yapmayı sağlar. Ayrıca, verileri gözlemleyebileceğiniz ve sulama işlemini manuel yapabildiğiniz bluetooth aracılığıyla çalışan bir Android uygulaması da bulunmaktadır.

---
### Android Uygulamasını İndirme:

Uygulamanın Son Sürümünü buradan indirebilirsiniz:

[![](https://img.shields.io/badge/Bitki%20Sulama-n1.6-green)](https://github.com/nyaexx/bitki-sulama/releases/tag/n1.6) 

- **Not:** Uygulama Android 7 ve üzeri için geliştirilmiştir.

### Android Uygulamasının Genel Özellikleri:
- Bluetooh ile cihazdan verilerin okunup ekrana yazılması.
- Manuel olarak su motorunun kontrol edilmesi.

---

### Android Uygulamasını Geliştirme:

**Gereklilikler:**

    Android Studio (Arctic Fox veya üzeri)

    Kotlin

    Gradle (Android Studio ile birlikte gelir)

    Min SDK: 24, Target SDK: 36 (Release n1.6 ve öncesi için Target SDK: 35'dir)

    Fiziksel Android cihaz (Bluetooth özelliklerinin kullanılması için gereklidir.)

**Nasıl Çalıştırılır?**

- Repo'yu klonla:

  -  ``git clone https://github.com/nyaexx/bitki-sulama/``

- Android Studio ile aç.

- Gradle sync tamamlandıktan sonra build edip fiziksel cihazına yükle.

- Uygulama yüklendiğinde önce Bluetooth ayarlarından cihazına bağlanıp daha sonra uygulamadan cihazı seçip yönetim sayfasına geçiş yapabilir ve sistemi kontrol edebilirsin. (Eğer cihaza bağlanırken sorun çıkarsa Ayarlar > Uygulamalar > Bitki Sulama > İzinler kısmından Bluetooth cihazlarına bağlanma ve konum izinlerini verdiğinizden emin olun.)

---

### Devre Kurulumu ve Kullanımı
Kurulum için aşağıdaki bağlantı şemasına göre devreyi kurun:
<p align="center">
  <img src="https://github.com/nyaexx/bitki-sulama/blob/main/Arduino%20Kodlar%C4%B1/ba%C4%9Flanti%C5%9Femas%C4%B1.png" width="">
</p>

> [!NOTE]
> Kullanım içinse [bu dizindeki](https://github.com/nyaexx/bitki-sulama/tree/main/Arduino%20Kodlar%C4%B1) Arduino kodlarını ve libraries klasöründeki kütüphaneleri cihazınıza yükleyip kullanabilirsiniz.
>
> Unutmayın eğer orjinal Arduino Uno kullanmıyorsanız kesinlikle [Arduino Kodları](https://github.com/nyaexx/bitki-sulama/tree/main/Arduino%20Kodlar%C4%B1) dinizindeki libraries klasöründe bulunan CH340 kütüphanesinin kurulumunu yapınız. Aksi takdirde kurulumunu yapmanıza gerek yoktur.

> [!CAUTION]
> ``Bluetooht Bağlantısı İçin Yapılması Gerekenler:``
**İlk bağlantıda telefonun bluetooth bağlantı menüsü açılıp "HC-06" seçilip şifre 1234 girilmelidir.
Uygulama içinden bağlantıda bağlantı sorunu yaşanırsa uno kart üzerinde bulunan kırmızı reset düğmesine basıldıktan hemen sonra eşleşme yapılmalıdır.
Bluetooth modül üzerinde bulunan kırmızı led yanıp söndüğünde "cihaz aranıyor" sürekli yandığında "bağlantı sağlandı" manasına gelir.
Eğer bluetooth bağlı gözükürken eşleşme yapılamazsa devrede elektriği kesin ışıklar sönene kadar bekleyin ve tekrar elektriği bağlayın sorun düzelecektir.**



---

> [!NOTE]
> Yardım veya herhangi bir soru için discord: nyaex
> 
> Repoya bir şey eklemek ister veya bir hatayı düzeltmek isterseniz pull request atabilirsiniz.

