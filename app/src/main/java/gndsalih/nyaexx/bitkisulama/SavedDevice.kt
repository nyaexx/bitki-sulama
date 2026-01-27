package gndsalih.nyaexx.bitkisulama

/**
 * Kayıtlı cihaz bilgilerini tutan veri modeli.
 * @param name: Kullanıcının cihaza verdiği özel isim.
 * @param address: Cihazın benzersiz MAC adresi (Bağlantı için kullanılır).
 * @param originalName: Cihazın fabrikasyon Bluetooth adı.
 */
data class SavedDevice(
    val name: String,
    val address: String,
    val originalName: String
)