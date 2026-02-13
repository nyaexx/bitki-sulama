<div align="center">
<p>
<img src="https://github.com/nyaexx/bitki-sulama/blob/main/.github/biktisulamayenilogo.png" width="150px">
</p>
<h1>Akıllı Sulama</h1>
<p>
<a href="README.md">English</a> 
| Türkçe
</p>
<p>
<a href="https://github.com/nyaexx/bitki-sulama/releases/latest"><img src="https://img.shields.io/github/v/release/nyaexx/bitki-sulama?display_name=tag&label=Ak%C4%B1ll%C4%B1%20Sulama" alt="Latest Release" /></a>
<a href="https://github.com/nyaexx/bitki-sulama/releases"><img alt="GitHub Downloads (all assets, all releases)" src="https://img.shields.io/github/downloads/nyaexx/bitki-sulama/total?label=%C4%B0ndirmeler" /></a>
<a href="https://github.com/nyaexx/bitki-sulama/commits/main/"><img alt="GitHub commits since latest release" src="https://img.shields.io/github/commits-since/nyaexx/bitki-sulama/latest?label=Son%20S%C3%BCr%C3%BCmden%20Beri%20Commit'ler" /></a>
<a href="https://github.com/nyaexx/bitki-sulama/blob/master/LICENSE"><img src="https://img.shields.io/github/license/nyaexx/bitki-sulama?label=Lisans" alt="License" /></a>
</p>
</div>

Bu proje, belirli bir bitkinin toprak nemi ve hava değerlerini ölçerek bir Arduino devresiyle otomatik sulama işlemleri yapmayı sağlar. Ayrıca, verileri gözlemleyebileceğiniz ve sulama işlemini manuel yapabildiğiniz bluetooth aracılığıyla çalışan bir Android uygulaması da bulunmaktadır.

---

### Android Uygulamasının Genel Özellikleri:
- Cihazlarım sekmesine birden fazla cihaz ekleme.
- Cihazları silme ve düzenleme.
- Cihazlarım sayfasından ekli cihazlarınızla bağlantı kurup yönetim paneline erişim.
- Yönetim paneli üzerinden cihazınızdan sensör verilerinin okunabilmesi.
- Yönetim paneli üzerinden sulama motorunun otomatik tetiklenebilmesi.
- Ayarlar kısmından uygulama temasını özelleştirme.

### Devrenin İşleyişi Özellikleri:

- İşleyiş:

  - Ölçüm: Sensörler toprağın nemini, havanın sıcaklığını ve nemini ölçer.
  - Karar: Toprak kuruduğunda sistem bunu algılar, kırmızı LED'i yakar ve su pompasını (röleyi) otomatik olarak çalıştırır.
     - Kullandığınız bitki türünün su isteğine göre Arduino kodundaki [**void loop()**](https://github.com/nyaexx/bitki-sulama/blob/092b0fe9b20702ee34cc0e6b0391c396f2d98dd1/Arduino%20Kodlar%C4%B1/arduinokodlar%C4%B1.ino#L53) içinde bulunan nem_orani eşik değerlerini düzenlemeniz önerilir.
  - Bilgilendirme: Toprağın durumu (Kuru, Normal, Islak) LED'ler aracılığıyla gösterilir ve tüm veriler Bluetooth ile telefona gönderilir.
  - Uzaktan Kontrol: İstenildiği zaman Bluetooth bağlantısıyla uygulama üzerinden tek bir komutla manuel sulama başlatılabilir.

- Temel Özellikler:

  - Tam Otomatik Sulama: Kimse yokken bitkileri kendi kendine sular.
  - Bluetooth Takibi: Telefon üzerinden anlık sıcaklık ve nem takibi sağlar.
  - Durum Göstergeleri: LED'ler sayesinde toprağın suya ihtiyacı olup olmadığını anında gösterir.

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

### Devre Kurulumu ve Kullanımı:
Kurulum için aşağıdaki bağlantı şemasına göre devreyi kurun:
<p align="center">
  <img src="https://github.com/nyaexx/bitki-sulama/blob/main/Arduino%20Kodlar%C4%B1/ba%C4%9Flanti%C5%9Femas%C4%B1.png" width="">
</p>

> [!NOTE]
> Kullanım içinse [bu dizindeki](https://github.com/nyaexx/bitki-sulama/tree/main/Arduino%20Kodlar%C4%B1) Arduino kodlarını ve libraries klasöründeki kütüphaneleri cihazınıza yükleyip kullanabilirsiniz.
>
> Unutmayın eğer orjinal Arduino Uno kullanmıyorsanız kesinlikle [Arduino Kodları](https://github.com/nyaexx/bitki-sulama/tree/main/Arduino%20Kodlar%C4%B1) dinizindeki libraries klasöründe bulunan CH340 kütüphanesinin kurulumunu yapınız. Aksi takdirde kurulumunu yapmanıza gerek yoktur.

> [!IMPORTANT]
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
