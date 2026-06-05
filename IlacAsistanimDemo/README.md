# İlaç Asistanım Demo

Bu klasör Android Studio'da açılacak demo mobil uygulama projesidir. Uygulama SRS 1.0 ve SDD 1.0 dosyalarındaki ana hasta/sorumlu akışlarını gerçek backend olmadan, yerel demo verisiyle gösterir.

## Çalıştırma

1. Android Studio'yu açın.
2. `Open` ile bu klasörü seçin: `IlacAsistanimDemo`.
3. Gradle Sync tamamlanınca bir emulator veya bağlı Android cihaz seçin.
4. `Run` butonuna basın.

Android Studio eksik SDK veya Gradle bileşeni isterse `Install` / `Download` onayı verin. Proje dış kütüphane kullanmaz; sadece Android Gradle Plugin ve standart Android SDK gerekir.

## Demo hesaplar

| Rol | E-posta | Şifre |
| --- | --- | --- |
| Hasta | `hasta.demo@ilac.app` | `123456` |
| Sorumlu | `sorumlu1.demo@ilac.app` | `123456` |
| Sorumlu | `sorumlu2.demo@ilac.app` | `123456` |

## Demo özellikleri

- 30 ilaçlık hazır katalog
- Hasta paneli ve sorumlu paneli
- Bağlantı kodu: `QWERTY123`
- İlaç ekleme, ilaç çıkarma, doz onayı ve düşük stok uyarısı
- Takvim görünümü
- Semptom ekleme ve sorumlu bildirim günlüğü
- ACİL butonu ve demo acil bildirim kaydı
- Kaçırılan doz için demo bildirim üretme

Bu sürüm sunum/demo amaçlıdır. Firebase, GPS geofence, ödeme ve backend servisleri gerçek entegrasyon yerine uygulama içi sahte veriyle temsil edilir.
