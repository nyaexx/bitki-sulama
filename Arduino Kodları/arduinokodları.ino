#include <SoftwareSerial.h>

// Sensör pinlerinin tanımlanması
#define DHTPIN 3
#define DHTTYPE DHT11
DHT dht(DHTPIN, DHTTYPE);
SoftwareSerial mySerial(10, 11);  // RX, TX

// Pin ataması
const int led1 = 4;
const int led2 = 5;
const int led3 = 6;
const int role_pin = 2;
const int nem_sensor = A0;

// Değişken tanımlamaları
int nem_orani;
String toprak_durumu;
float t;
float h;
String inputString = "";         // Bluetooth'tan gelen komut için string
boolean stringComplete = false;  // String tamamlandı mı?

void setup() {
    // Seri haberleşme başlatılması ve pin durumları ayarlanması
    mySerial.begin(9600);
    Serial.begin(9600);
    dht.begin();
    pinMode(nem_sensor, INPUT);
    pinMode(led1, OUTPUT);
    pinMode(led2, OUTPUT);
    pinMode(led3, OUTPUT);
    pinMode(role_pin, OUTPUT);
    digitalWrite(role_pin, HIGH);  // Röle başlangıçta KAPALI

    // Başlangıç LED selamlaması
    for (int a = 0; a < 3; a++) {
        digitalWrite(led1, HIGH);
        delay(80);
        digitalWrite(led2, HIGH);
        delay(80);
        digitalWrite(led3, HIGH);
        delay(80);
        digitalWrite(led1, LOW);
        delay(80);
        digitalWrite(led2, LOW);
        delay(80);
        digitalWrite(led3, LOW);
        delay(80);
    }
}

void loop() {
    // Bluetooth'tan gelen veriyi kontrol et
    checkBluetoothCommands();
    
    // DHT11'den nem ve sıcaklık değerinin okunması
    t = dht.readTemperature();
    h = dht.readHumidity();

    // Toprak nem sensöründen değerin okunması ve uygun işlemlerin yaptırılması
    nem_orani = analogRead(nem_sensor);
    
    if (nem_orani > 600) {
        toprak_durumu = "Kuru";
        digitalWrite(led2, LOW);
        digitalWrite(led3, LOW);
        digitalWrite(led1, HIGH);
        
        // Otomatik sulama işlemi (Kuru olduğunda)
        digitalWrite(role_pin, LOW);
        delay(900);
        digitalWrite(role_pin, HIGH);
        delay(1500);
    } else if (nem_orani > 400) {
        toprak_durumu = "Normal";
        digitalWrite(led1, LOW);
        digitalWrite(led3, LOW);
        digitalWrite(led2, HIGH);
        digitalWrite(role_pin, HIGH);
        delay(100);
    } else {
        toprak_durumu = "Çok Islak";
        digitalWrite(led1, LOW);
        digitalWrite(led2, LOW);
        digitalWrite(led3, HIGH);
        digitalWrite(role_pin, HIGH);
        delay(100);
    }

    // Seri haberleşme ile APK uygulamaya okunan değerleri gönderme
    mySerial.print("Sıcaklık: ");
    mySerial.print(t, 1);
    mySerial.println("°C");
    
    mySerial.print("|");
    
    mySerial.print("Toprak Nemi: ");
    mySerial.println(toprak_durumu);
    
    mySerial.print("|");
    
    mySerial.print("Nem: ");
    mySerial.print("%");
    mySerial.println(h, 1);

    // Seri port ekranına değerleri yazdırma
    Serial.print("Sıcaklık: ");
    Serial.print(t, 1);
    Serial.println("°C");
    
    Serial.print("Toprak Nemi: ");
    Serial.println(toprak_durumu);
      
    Serial.print("Nem: ");
    Serial.print("%");
    Serial.println(h, 1);

    Serial.println("----------------------------");
    
    delay(500); // Verileri göndermeden önce 500ms bekle
}

// Bluetooth'tan gelen komutları kontrol et
void checkBluetoothCommands() {
    while (mySerial.available()) {
        char inChar = (char)mySerial.read();
        inputString += inChar;
        
        // Eğer yeni satır karakteri gelirse komut tamamlanmış demektir
        if (inChar == '\n') {
            stringComplete = true;
        }
    }
    
    // Eğer komut tamamlanmışsa işle
    if (stringComplete) {
        // "WATER" komutu gelirse manuel sulama yap
        if (inputString.indexOf("WATER") != -1) {
            manualWatering();
        }
        
        // Komutu temizle ve yeni komut için hazırlık yap
        inputString = "";
        stringComplete = false;
    }
}

// Manuel sulama fonksiyonu
void manualWatering() {
    Serial.println("Manuel sulama başlatıldı");
    
    // Röleyi aktifleştir (LOW = AÇIK)
    digitalWrite(role_pin, LOW);
    
    // Sulama süresini belirle (1 saniye)
    delay(1000);
    
    // Röleyi kapat (HIGH = KAPALI)
    digitalWrite(role_pin, HIGH);
    
    Serial.println("Manuel sulama tamamlandı");
}
