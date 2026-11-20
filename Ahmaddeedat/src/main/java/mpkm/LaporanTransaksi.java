package mpkm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

public class LaporanTransaksi {

    private static final String URL = "jdbc:mysql://localhost:3306/mpkm";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public void tampilkanLaporan() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

            // Sesuaikan path file LaporanTransaksi.jrxml jika berada di folder berbeda
            String jrxmlPath = "src/main/resources/reports/LaporanTransaksi.jrxml";
            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlPath);

            Map<String, Object> parameters = new HashMap<>();
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);

            JasperViewer.viewReport(jasperPrint, false);
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new LaporanTransaksi().tampilkanLaporan();
    }
}