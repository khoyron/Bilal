# Deteksi Nama Kota Menggunakan Library Compass Geocoder

Rencana untuk mengintegrasikan library `Compass Geocoder` ke dalam `QiblahViewModel` agar nama lokasi pengguna dapat ditampilkan secara otomatis (misal: "Mojokerto") daripada hanya angka koordinat.

## Proposed Changes

### [Component] ViewModel
#### [MODIFY] [QiblahViewModel.kt](file:///Users/moehammadkhoyron/DriverApps/Bilal/composeApp/src/commonMain/kotlin/org/khoyron/bilal/ui/qiblah/QiblahViewModel.kt)
*   Menambahkan import:
    *   `androidx.lifecycle.viewModelScope`
    *   `dev.jordond.compass.geocoder.Geocoder`
    *   `dev.jordond.compass.geocoder.GeocoderResult`
    *   `kotlinx.coroutines.launch`
*   Menambahkan fungsi `updateLocationName(lat: Double, lon: Double)` yang akan melakukan reverse geocoding secara asinkron.
*   Memperbarui `handleLocation` untuk memanggil `updateLocationName`.
*   Memberikan fallback ke format koordinat jika proses geocoding gagal atau tidak ada internet.

## Verification Plan

### Manual Verification
*   Deploy aplikasi ke iOS Device/Simulator.
*   Tunggu hingga lokasi terdeteksi.
*   Pastikan kartu lokasi menampilkan nama kota (misalnya: "Mojokerto") alih-alih angka koordinat.
*   Matikan internet dan pastikan aplikasi tetap menampilkan koordinat sebagai fallback (tidak crash).
