package mpkm;

import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import javax.swing.JOptionPane;
import java.util.List;

public class pelanggan extends javax.swing.JPanel {

    @Entity
    @Table(name = "pelanggan")
    public static class Pelanggan {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        private String nama;
        private String alamat;
        private String telp;

        public Pelanggan() {
        }

        public Pelanggan(String nama, String alamat, String telp) {
            this.nama = nama;
            this.alamat = alamat;
            this.telp = telp;
        }
    }

    private static final SessionFactory factory =
            new Configuration()
                    .configure("hibernate.cfg.xml")
                    .addAnnotatedClass(Pelanggan.class)
                    .buildSessionFactory();

    public static class PelangganDAO {

        public void simpan(Pelanggan pelanggan) {
            Transaction tx = null;
            try (Session session = factory.openSession()) {
                tx = session.beginTransaction();
                session.persist(pelanggan);
                tx.commit();
            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw new RuntimeException("Gagal menyimpan data: " + e.getMessage(), e);
            }
        }
        
    public List<Pelanggan> ambilSemua() {
    try (Session session = factory.openSession()) {
        return session.createQuery(
            "FROM pelanggan", Pelanggan.class
        ).list();
    } catch (Exception e) {
        throw new RuntimeException(
            "Gagal mengambil data pelanggan: " + e.getMessage(), e
        );
    }
}

        public boolean edit(int id, String nama, String alamat, String telp) {
            Transaction tx = null;
            try (Session session = factory.openSession()) {
                tx = session.beginTransaction();
                Pelanggan p = session.get(Pelanggan.class, id);

                if (p == null) {
                    tx.rollback();
                    return false;
                }

                p.nama = nama;
                p.alamat = alamat;
                p.telp = telp;
                tx.commit();
                return true;
            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw new RuntimeException("Gagal mengedit data: " + e.getMessage(), e);
            }
        }

        public boolean hapus(int id) {
            Transaction tx = null;
            try (Session session = factory.openSession()) {
                tx = session.beginTransaction();
                Pelanggan p = session.get(Pelanggan.class, id);

                if (p == null) {
                    tx.rollback();
                    return false;
                }

                session.remove(p);
                tx.commit();
                return true;
            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw new RuntimeException("Gagal menghapus data: " + e.getMessage(), e);
            }
        }
    }

    private final PelangganDAO pelangganDAO = new PelangganDAO();

    public pelanggan() {
        initComponents();
        jButton1.addActionListener(e -> simpan());
        jButton2.addActionListener(e -> edit());
        jButton3.addActionListener(e -> hapus());
    }

    private void simpan() {
        String nama = textField1.getText().trim();
        String alamat = textField2.getText().trim();
        String telp = textField3.getText().trim();

        if (nama.isEmpty() || alamat.isEmpty() || telp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua data harus diisi!");
            return;
        }

        try {
            Pelanggan p = new Pelanggan(nama, alamat, telp);
            pelangganDAO.simpan(p);
            JOptionPane.showMessageDialog(this, "Data pelanggan berhasil disimpan!");
            textField1.setText("");
            textField2.setText("");
            textField3.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void edit() {
        String idInput = JOptionPane.showInputDialog(this,
                "Masukkan ID pelanggan yang ingin diedit:");
        if (idInput == null || idInput.trim().isEmpty()) {
            return;
        }

        String nama = textField1.getText().trim();
        String alamat = textField2.getText().trim();
        String telp = textField3.getText().trim();

        if (nama.isEmpty() || alamat.isEmpty() || telp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi nama, alamat, dan telp terlebih dahulu!");
            return;
        }

        try {
            int id = Integer.parseInt(idInput.trim());
            boolean berhasil = pelangganDAO.edit(id, nama, alamat, telp);
            if (berhasil) {
                JOptionPane.showMessageDialog(this, "Data pelanggan berhasil diedit!");
            } else {
                JOptionPane.showMessageDialog(this, "Data pelanggan tidak ditemukan!");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID harus berupa angka!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void hapus() {
        String idInput = JOptionPane.showInputDialog(this,
                "Masukkan ID pelanggan yang ingin dihapus:");
        if (idInput == null || idInput.trim().isEmpty()) {
            return;
        }

        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Yakin ingin menghapus data pelanggan?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION);
        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int id = Integer.parseInt(idInput.trim());
            boolean berhasil = pelangganDAO.hapus(id);
            if (berhasil) {
                JOptionPane.showMessageDialog(this, "Data pelanggan berhasil dihapus!");
                textField1.setText("");
                textField2.setText("");
                textField3.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Data pelanggan tidak ditemukan!");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID harus berupa angka!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        textField1 = new java.awt.TextField();
        textField2 = new java.awt.TextField();
        textField3 = new java.awt.TextField();
        label1 = new java.awt.Label();
        label2 = new java.awt.Label();
        label3 = new java.awt.Label();
        label4 = new java.awt.Label();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        label1.setText("PELANGGAN");

        label2.setText("ALAMAT");

        label3.setText("NAMA");

        label4.setText("TELP");

        jButton1.setText("Simpan");

        jButton2.setText("Edit");

        jButton3.setText("Hapus");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(59, 59, 59)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(label2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(label4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(label3, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE))
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(textField3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(textField2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(textField1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(label1, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addGap(40, 40, 40)
                        .addComponent(jButton3)))
                .addContainerGap(45, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(56, 56, 56)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(textField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textField3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addGap(36, 36, 36))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private java.awt.Label label1;
    private java.awt.Label label2;
    private java.awt.Label label3;
    private java.awt.Label label4;
    private java.awt.TextField textField1;
    private java.awt.TextField textField2;
    private java.awt.TextField textField3;
    // End of variables declaration//GEN-END:variables
}