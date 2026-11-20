/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package mpkm;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author user
 */
public class pembayaran extends javax.swing.JFrame {

    private static final String URL = "jdbc:mysql://localhost:3306/mpkm";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Integer currentIdPembayaran = null;

    /**
     * Creates new form pembayaran
     */
    public pembayaran() {
        initComponents();
        initCustomLogic();
    }

    private Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private void initCustomLogic() {
        jComboBox2.setModel(new DefaultComboBoxModel<>(new String[]{"Tunai", "Transfer", "QRIS"}));
        jComboBox3.setModel(new DefaultComboBoxModel<>(new String[]{"Belum Lunas", "Lunas"}));

        jTextField1.setEditable(false); // Nama Pelanggan
        jTextField3.setEditable(false); // Total Biaya
        jTextField5.setEditable(false); // Sisa Pembayaran

        loadIdTransaksi();
        clearForm();

        jComboBox1.addActionListener(e -> onTransaksiSelected());

        jTextField4.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                hitungSisaDanStatus();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                hitungSisaDanStatus();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                hitungSisaDanStatus();
            }
        });
        jButton1.addActionListener(e -> simpanPembayaran());
        jButton2.addActionListener(e -> editPembayaran());
        jButton3.addActionListener(e -> hapusPembayaran());
        jButton4.addActionListener(e -> clearForm());
    }

    private void loadIdTransaksi() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        String sql = "SELECT id_transaksi FROM transaksi ORDER BY id_transaksi ASC";

        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                model.addElement(String.valueOf(rs.getInt("id_transaksi")));
            }
            jComboBox1.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat ID Transaksi: " + e.getMessage());
        }
    }

    private void onTransaksiSelected() {
        Object selected = jComboBox1.getSelectedItem();
        if (selected == null) {
            return;
        }

        int idTransaksi = Integer.parseInt(selected.toString());
        String sqlTransaksi = "SELECT t.total_biaya, p.nama "
                + "FROM transaksi t "
                + "JOIN pelanggan p ON t.id_pelanggan = p.id "
                + "WHERE t.id_transaksi = ?";

        String sqlPembayaran = "SELECT id_pembayaran, tanggal_bayar, jumlah_bayar, sisa_pembayaran, metode_pembayaran, status "
                + "FROM pembayaran "
                + "WHERE id_transaksi = ? "
                + "ORDER BY id_pembayaran DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement pstT = conn.prepareStatement(sqlTransaksi)) {

            pstT.setInt(1, idTransaksi);
            try (ResultSet rsT = pstT.executeQuery()) {
                if (rsT.next()) {
                    jTextField1.setText(rsT.getString("nama") != null ? rsT.getString("nama") : "");
                    BigDecimal totalBiaya = rsT.getBigDecimal("total_biaya");
                    if (totalBiaya == null) {
                        totalBiaya = BigDecimal.ZERO;
                    }
                    jTextField3.setText(totalBiaya.toPlainString());
                    jTextField5.setText(totalBiaya.toPlainString());
                }
            }

            try (PreparedStatement pstP = conn.prepareStatement(sqlPembayaran)) {
                pstP.setInt(1, idTransaksi);
                try (ResultSet rsP = pstP.executeQuery()) {
                    if (rsP.next()) {
                        currentIdPembayaran = rsP.getInt("id_pembayaran");
                        jTextField2.setText(rsP.getDate("tanggal_bayar").toString());
                        jTextField4.setText(rsP.getBigDecimal("jumlah_bayar").toPlainString());
                        jTextField5.setText(rsP.getBigDecimal("sisa_pembayaran").toPlainString());

                        String metode = rsP.getString("metode_pembayaran");
                        if (metode != null) {
                            jComboBox2.setSelectedItem(metode);
                        }

                        String status = rsP.getString("status");
                        if (status != null) {
                            jComboBox3.setSelectedItem(status);
                        }
                    } else {
                        currentIdPembayaran = null;
                        jTextField2.setText(LocalDate.now().toString());
                        jTextField4.setText("0.00");
                        hitungSisaDanStatus();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat detail transaksi: " + e.getMessage());
        }
    }

    private BigDecimal parseDecimal(String text) {
        if (text == null || text.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(text.trim().replace(",", "."));
    }

    private void hitungSisaDanStatus() {
        try {
            BigDecimal totalBiaya = parseDecimal(jTextField3.getText());
            BigDecimal jumlahBayar = parseDecimal(jTextField4.getText());
            BigDecimal sisa = totalBiaya.subtract(jumlahBayar);
            if (sisa.compareTo(BigDecimal.ZERO) < 0) {
                sisa = BigDecimal.ZERO;
            }
            jTextField5.setText(sisa.toPlainString());

            if (sisa.compareTo(BigDecimal.ZERO) == 0 && totalBiaya.compareTo(BigDecimal.ZERO) > 0) {
                jComboBox3.setSelectedItem("Lunas");
            } else {
                jComboBox3.setSelectedItem("Belum Lunas");
            }
        } catch (NumberFormatException ex) {
            // Abaikan saat user sedang mengetik karakter angka/titik
        }
    }

    private void simpanPembayaran() {
        Object selected = jComboBox1.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Pilih ID Transaksi terlebih dahulu!");
            return;
        }

        try {
            int idTransaksi = Integer.parseInt(selected.toString());
            Date tanggalBayar = Date.valueOf(jTextField2.getText().trim());
            BigDecimal jumlahBayar = parseDecimal(jTextField4.getText());
            BigDecimal sisaPembayaran = parseDecimal(jTextField5.getText());
            String metodePembayaran = jComboBox2.getSelectedItem().toString();
            String status = jComboBox3.getSelectedItem().toString();

            String sql = "INSERT INTO pembayaran (id_transaksi, tanggal_bayar, jumlah_bayar, sisa_pembayaran, metode_pembayaran, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection conn = getConnection();
                 PreparedStatement pst = conn.prepareStatement(sql)) {

                pst.setInt(1, idTransaksi);
                pst.setDate(2, tanggalBayar);
                pst.setBigDecimal(3, jumlahBayar);
                pst.setBigDecimal(4, sisaPembayaran);
                pst.setString(5, metodePembayaran);
                pst.setString(6, status);

                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Data pembayaran berhasil disimpan!");
                onTransaksiSelected();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Format Tanggal Bayar harus YYYY-MM-DD dan angka harus valid!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menyimpan pembayaran: " + e.getMessage());
        }
    }

    private void editPembayaran() {
        if (currentIdPembayaran == null) {
            JOptionPane.showMessageDialog(this, "Belum ada data pembayaran tersimpan untuk transaksi ini. Gunakan tombol Simpan.");
            return;
        }

        try {
            Date tanggalBayar = Date.valueOf(jTextField2.getText().trim());
            BigDecimal jumlahBayar = parseDecimal(jTextField4.getText());
            BigDecimal sisaPembayaran = parseDecimal(jTextField5.getText());
            String metodePembayaran = jComboBox2.getSelectedItem().toString();
            String status = jComboBox3.getSelectedItem().toString();

            String sql = "UPDATE pembayaran "
                    + "SET tanggal_bayar = ?, jumlah_bayar = ?, sisa_pembayaran = ?, metode_pembayaran = ?, status = ? "
                    + "WHERE id_pembayaran = ?";

            try (Connection conn = getConnection();
                 PreparedStatement pst = conn.prepareStatement(sql)) {

                pst.setDate(1, tanggalBayar);
                pst.setBigDecimal(2, jumlahBayar);
                pst.setBigDecimal(3, sisaPembayaran);
                pst.setString(4, metodePembayaran);
                pst.setString(5, status);
                pst.setInt(6, currentIdPembayaran);

                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Data pembayaran berhasil diperbarui!");
                onTransaksiSelected();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Format Tanggal Bayar harus YYYY-MM-DD dan angka harus valid!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengedit pembayaran: " + e.getMessage());
        }
    }

    private void hapusPembayaran() {
        if (currentIdPembayaran == null) {
            JOptionPane.showMessageDialog(this, "Tidak ada data pembayaran untuk dihapus!");
            return;
        }

        int konfirmasi = JOptionPane.showConfirmDialog(
                this,
                "Yakin ingin menghapus data pembayaran ini?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION
        );

        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM pembayaran WHERE id_pembayaran = ?";

        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, currentIdPembayaran);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data pembayaran berhasil dihapus!");
            currentIdPembayaran = null;
            jTextField4.setText("0.00");
            hitungSisaDanStatus();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menghapus pembayaran: " + e.getMessage());
        }
    }

    private void clearForm() {
        currentIdPembayaran = null;
        jTextField2.setText(LocalDate.now().toString());
        jTextField4.setText("0.00");
        if (jComboBox2.getItemCount() > 0) {
            jComboBox2.setSelectedIndex(0);
        }
        if (jComboBox1.getItemCount() > 0) {
            jComboBox1.setSelectedIndex(0);
            onTransaksiSelected();
        } else {
            jTextField1.setText("");
            jTextField3.setText("0.00");
            jTextField5.setText("0.00");
            jComboBox3.setSelectedItem("Belum Lunas");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToggleButton1 = new javax.swing.JToggleButton();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jComboBox2 = new javax.swing.JComboBox<>();
        jComboBox3 = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        jToggleButton1.setText("jToggleButton1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Pembayaran");

        jLabel2.setText("ID Transaksi");

        jLabel3.setText("Nama Pelanggan");

        jLabel4.setText("Tanggal Bayar");

        jLabel5.setText("Total Biaya");

        jLabel6.setText("Jumlah Dibayar");

        jLabel7.setText("Sisa Pembayaran");

        jLabel8.setText("Metode Bayar");

        jLabel10.setText("Status");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButton1.setText("Simpan");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Edit");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Hapus");

        jButton4.setText("Clear");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(202, 202, 202))
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(84, 84, 84))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE))
                        .addGap(52, 52, 52))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(84, 84, 84))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(139, 139, 139)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton1)
                                .addGap(18, 18, 18)
                                .addComponent(jButton2))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton3)
                                .addGap(18, 18, 18)
                                .addComponent(jButton4)))
                        .addGap(0, 71, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(22, 22, 22)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton3)
                            .addComponent(jButton4))))
                .addGap(29, 29, 29))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

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
            java.util.logging.Logger.getLogger(pembayaran.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(pembayaran.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(pembayaran.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(pembayaran.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new pembayaran().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JToggleButton jToggleButton1;
    // End of variables declaration//GEN-END:variables
}
