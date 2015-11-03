package com.indonative.cari_darah.model;

/**
 * Created by andika on 11/2/15.
 */
public class PMIBranchModel {
    private String kode_cabang;
    private int jumlah_stok;
    private String nama_cabang;
    private Double longitude;
    private Double latitude;
    private String alamat;

    public PMIBranchModel(String kode_cabang, int jumlah_stok, String nama_cabang, Double longitude, Double latitude, String alamat) {
        this.kode_cabang = kode_cabang;
        this.jumlah_stok = jumlah_stok;
        this.nama_cabang = nama_cabang;
        this.longitude = longitude;
        this.latitude = latitude;
        this.alamat = alamat;
    }

    public String getKodeCabang() {
        return kode_cabang;
    }

    public int getJumlahStok() {
        return jumlah_stok;
    }

    public String getNamaCabang() {
        return nama_cabang;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public String getAlamat() {
        return alamat;
    }
    public String getAll() {
        return kode_cabang + String.valueOf(jumlah_stok) + nama_cabang + String.valueOf(longitude)
                + String.valueOf(latitude) + alamat;
    }
}
