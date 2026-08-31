# Walkthrough Optimalisasi Tampilan Lokasi

Saya telah mengoptimalkan cara pembaruan nama lokasi untuk mencegah tampilan yang berkedip antara koordinat dan nama kota.

## Perubahan yang Dilakukan

### 1. Pembatasan Pembaruan Geocoding
*   **File:** [QiblahViewModel.kt](file:///Users/moehammadkhoyron/DriverApps/Bilal/composeApp/src/commonMain/kotlin/org/khoyron/bilal/ui/qiblah/QiblahViewModel.kt)
*   **Logika Baru:**
    *   Aplikasi sekarang menyimpan posisi terakhir saat nama kota berhasil ditemukan.
    *   Nama kota **hanya akan diperbarui** jika Anda berpindah lokasi lebih dari **1 km** (sekitar 0.01 derajat) dari posisi terakhir.
    *   Jika sudah ada nama kota dan Anda masih berada dalam radius 1 km, aplikasi **tidak akan lagi** menampilkan koordinat mentah secara sekilas, sehingga tampilan tetap stabil.

### 2. Force Refresh via Tombol
*   Jika Anda merasa nama lokasi kurang akurat, menekan tombol **Get Location** (ikon target) akan memaksa aplikasi untuk mencari ulang nama kota tanpa mempedulikan batas 1 km tersebut.

## Hasil
Tampilan kartu lokasi sekarang akan jauh lebih stabil. Setelah nama kota ditemukan (misal: "Mojokerto"), teks tersebut akan menetap di sana dan tidak akan berubah kembali menjadi angka koordinat kecuali Anda melakukan perjalanan yang cukup jauh atau menekan tombol refresh secara manual.

> [!NOTE]
> Dengan optimasi ini, penggunaan data internet juga menjadi lebih hemat karena aplikasi tidak melakukan permintaan geocoding terus-menerus ke server.
