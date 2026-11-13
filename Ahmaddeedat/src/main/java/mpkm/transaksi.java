/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package mpkm;

import jakarta.persistence.*;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.swing.JOptionPane;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class transaksi extends javax.swing.JFrame {
    @Entity(name = "TransaksiData")
    @Table(name = "transaksi")
    public static class TransaksiData {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id_transaksi")
        private int idTransaksi;

        @ManyToOne
        @JoinColumn(name = "id_pelanggan")
        private pelanggan.Pelanggan pelanggan;

        @Column(name = "jenis_layanan")
        private String jenisLayanan;

        @Column(name = "tanggal")
        private LocalDate tanggal;

        @Column(name = "biaya")
        private BigDecimal biaya;

        public TransaksiData() {
        }

        public void setPelanggan(pelanggan.Pelanggan pelanggan) {
            this.pelanggan = pelanggan;
        }

        public void setJenisLayanan(String jenisLayanan) {
            this.jenisLayanan = jenisLayanan;
        }

        public void setTanggal(LocalDate tanggal) {
            this.tanggal = tanggal;
        }

        public void setBiaya(BigDecimal biaya) {
            this.biaya = biaya;
        }
    }

    public static class TransaksiDAO {

        private final SessionFactory factory;

        public TransaksiDAO() {
            factory = pelanggan.getSessionFactory();
        }

        public void simpan(int idPelanggan, String layanan,
                           LocalDate tanggal, BigDecimal biaya) {

            Transaction tx = null;

            try (Session session = factory.openSession()) {

                tx = session.beginTransaction();

                pelanggan.Pelanggan p =
                        session.get(pelanggan.Pelanggan.class, idPelanggan);

                if (p == null) {
                    throw new RuntimeException("Pelanggan tidak ditemukan!");
                }

                TransaksiData data = new TransaksiData();
                data.setPelanggan(p);
                data.setJenisLayanan(layanan);
                data.setTanggal(tanggal);
                data.setBiaya(biaya);

                session.persist(data);
                tx.commit();

            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        }

        public void edit(int id, int idPelanggan, String layanan,
                         LocalDate tanggal, BigDecimal biaya) {

            Transaction tx = null;

            try (Session session = factory.openSession()) {

                tx = session.beginTransaction();

                TransaksiData data =
                        session.get(TransaksiData.class, id);

                if (data == null) {
                    throw new RuntimeException("Transaksi tidak ditemukan!");
                }

                pelanggan.Pelanggan p =
                        session.get(pelanggan.Pelanggan.class, idPelanggan);

                if (p == null) {
                    throw new RuntimeException("Pelanggan tidak ditemukan!");
                }

                data.setPelanggan(p);
                data.setJenisLayanan(layanan);
                data.setTanggal(tanggal);
                data.setBiaya(biaya);

                tx.commit();

            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        }

        public void hapus(int id) {

            Transaction tx = null;

            try (Session session = factory.openSession()) {

                tx = session.beginTransaction();

                TransaksiData data =
                        session.get(TransaksiData.class, id);

                if (data == null) {
                    throw new RuntimeException("Transaksi tidak ditemukan!");
                }

                session.remove(data);
                tx.commit();

            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }

    private final TransaksiDAO dao = new TransaksiDAO();

    private List<pelanggan.Pelanggan> daftarPelanggan =
            new java.util.ArrayList<>();

    private final String[] daftarLayanan = {
        "Pengelasan",
        "Pembubutan",
        "Pemotongan Besi",
        "Pembuatan Rangka Besi"
    };

    public transaksi() {
        initComponents();

        muatNamaPelanggan();
        muatLayanan();

        textField2.setEditable(false);

        jComboBox2.addActionListener(e -> hitungBiaya());

        jButton2.addActionListener(e -> editTransaksi());
        jButton3.addActionListener(e -> hapusTransaksi());
    }

    private void muatNamaPelanggan() {

        jComboBox1.removeAllItems();
        daftarPelanggan.clear();

        jComboBox1.addItem("Pilih Pelanggan");

        try {

            pelanggan.PelangganDAO daoPelanggan =
                    new pelanggan.PelangganDAO();

            daftarPelanggan = daoPelanggan.ambilSemua();

            for (pelanggan.Pelanggan p : daftarPelanggan) {
                jComboBox1.addItem(p.getNama());
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Gagal memuat pelanggan: " + e.getMessage()
            );
        }
    }

    private void muatLayanan() {

        jComboBox2.removeAllItems();

        jComboBox2.addItem("Pilih Layanan");

        for (String layanan : daftarLayanan) {
            jComboBox2.addItem(layanan);
        }
    }

    private void hitungBiaya() {

        String layanan =
                (String) jComboBox2.getSelectedItem();

        if (layanan == null) {
            return;
        }

        switch (layanan) {

            case "Pengelasan":
                textField2.setText("150000");
                break;

            case "Pembubutan":
                textField2.setText("200000");
                break;

            case "Pemotongan Besi":
                textField2.setText("100000");
                break;

            case "Pembuatan Rangka Besi":
                textField2.setText("350000");
                break;

            default:
                textField2.setText("");
                break;
        }
    }

    private void simpanTransaksi() {

        try {

            int index = jComboBox1.getSelectedIndex() - 1;

            if (index < 0 || index >= daftarPelanggan.size()) {
                JOptionPane.showMessageDialog(
                        this, "Pilih pelanggan terlebih dahulu!");
                return;
            }

            String layanan =
                    (String) jComboBox2.getSelectedItem();

            if (layanan == null || layanan.equals("Pilih Layanan")) {
                JOptionPane.showMessageDialog(
                        this, "Pilih jenis layanan!");
                return;
            }

            LocalDate tanggal =
                    LocalDate.parse(textField1.getText().trim());

            BigDecimal biaya =
                    new BigDecimal(textField2.getText().trim());

            if (biaya.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Biaya harus lebih dari nol!");
            }

            pelanggan.Pelanggan p = daftarPelanggan.get(index);

            dao.simpan(p.getId(), layanan, tanggal, biaya);

            JOptionPane.showMessageDialog(
                    this, "Transaksi berhasil disimpan!");

            bersihkanForm();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Gagal menyimpan transaksi: " + e.getMessage()
            );
        }
    }

    private void editTransaksi() {

        String input = JOptionPane.showInputDialog(
                this, "Masukkan ID transaksi yang ingin diedit:");

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        try {

            int id = Integer.parseInt(input.trim());

            int index = jComboBox1.getSelectedIndex() - 1;

            if (index < 0 || index >= daftarPelanggan.size()) {
                throw new IllegalArgumentException("Pilih pelanggan!");
            }

            String layanan = (String) jComboBox2.getSelectedItem();

            if (layanan == null || layanan.equals("Pilih Layanan")) {
                throw new IllegalArgumentException("Pilih layanan!");
            }

            LocalDate tanggal =
                    LocalDate.parse(textField1.getText().trim());

            BigDecimal biaya =
                    new BigDecimal(textField2.getText().trim());

            dao.edit(
                    id,
                    daftarPelanggan.get(index).getId(),
                    layanan,
                    tanggal,
                    biaya
            );

            JOptionPane.showMessageDialog(
                    this, "Transaksi berhasil diedit!");

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Gagal mengedit transaksi: " + e.getMessage()
            );
        }
    }

    private void hapusTransaksi() {

        String input = JOptionPane.showInputDialog(
                this, "Masukkan ID transaksi yang ingin dihapus:");

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        int konfirmasi = JOptionPane.showConfirmDialog(
                this,
                "Yakin ingin menghapus transaksi?",
                "Konfirmasi",
                JOptionPane.YES_NO_OPTION
        );

        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            int id = Integer.parseInt(input.trim());

            dao.hapus(id);

            JOptionPane.showMessageDialog(
                    this, "Transaksi berhasil dihapus!");

            bersihkanForm();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Gagal menghapus transaksi: " + e.getMessage()
            );
        }
    }

    private void bersihkanForm() {

        textField1.setText("");
        textField2.setText("");

        jComboBox1.setSelectedIndex(0);
        jComboBox2.setSelectedIndex(0);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        textField1 = new java.awt.TextField();
        jLabel5 = new javax.swing.JLabel();
        textField2 = new java.awt.TextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Transaksi");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel2.setText("Pelanggan");

        jLabel3.setText("Jenis Layanan");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel4.setText("Tanggal");

        textField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textField1ActionPerformed(evt);
            }
        });

        jLabel5.setText("Biaya");

        textField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textField2ActionPerformed(evt);
            }
        });

        jButton1.setText("Simpan");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Edit");

        jButton3.setText("Hapus");
        jButton3.setToolTipText("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(173, 173, 173)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton1)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel2)
                                .addComponent(jLabel3)
                                .addComponent(jLabel4)
                                .addComponent(jLabel5)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jComboBox2, 0, 92, Short.MAX_VALUE)
                            .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(textField1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(textField2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addComponent(jButton2)))))
                .addGap(27, 27, 27)
                .addComponent(jButton3)
                .addGap(57, 57, 57))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(textField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(textField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addContainerGap(69, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void textField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textField1ActionPerformed

    private void textField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textField2ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed
 
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    simpanTransaksi();    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new transaksi().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private java.awt.TextField textField1;
    private java.awt.TextField textField2;
    // End of variables declaration//GEN-END:variables
}