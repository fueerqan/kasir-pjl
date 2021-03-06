/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kafe;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/*############## IMPORT TAMBAHAN UNTUK KEPENTINGAN PDF ITEXT ############*/
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.net.URL;
import javax.swing.ImageIcon;
/*############## IMPORT TAMBAHAN UNTUK KEPENTINGAN PDF ITEXT ############*/

import static kafe.Koneksi.driver;
import static kafe.Koneksi.pass;
import static kafe.Koneksi.url;
import static kafe.Koneksi.user;

/**
 *
 * @author Maulana
 */
public class BayarSemuaFrame extends javax.swing.JFrame {

    /**
     * Creates new form BayarSemuaFrame
     */
    
    int id_pesanan;
    int id_meja;
    int total=0;
    
    private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
    private static Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
    private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
    private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
    
    public BayarSemuaFrame(int id_pesanan, int id_meja) {
        initComponents();
        this.id_pesanan=id_pesanan;
        this.id_meja=id_meja;
        ambilPesanan();
    }
    
    private void ambilPesanan() {
        try {
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, user, pass);
            Statement statement = connection.createStatement();
            String sql = "SELECT isi_pesanan.id_makanan, makanan.nama_makanan, makanan.harga_makanan*isi_pesanan.jumlah_makanan as harga_makanan, jumlah_makanan FROM isi_pesanan INNER JOIN makanan ON makanan.id_makanan=isi_pesanan.id_makanan where id_pesanan='" + id_pesanan + "';";
            ResultSet rs = statement.executeQuery(sql);

            jTable1.getSelectionModel().addListSelectionListener(
                    new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent event) {
                    int viewRow = jTable1.getSelectedRow();
                    if (viewRow < 0) {
                        //Selection got filtered away.
                    } else {
//                        jComboBox1.setSelectedItem("" + jTable1.getValueAt(viewRow, 1));
//                        txt_id.setText("" + jTable1.getValueAt(viewRow, 0));
//                        ambilHarga();
//                        txt_jumlah.setText("" + jTable1.getValueAt(viewRow, 2));
//                        
//                        //jButton1.setEnabled(false);
////                        jButton2.setEnabled(true);
////                        jButton3.setEnabled(true);
                    }
                }
            }
            );
            TablePesananModel model= new TablePesananModel(rs);
            jTable1.setModel(model);
            total=model.getTotal();
            
            txt_total.setText(""+total);

            pack();

            rs.close();
            statement.close();
            connection.close();
        } catch (Exception DBException) {
            
            System.out.println("Error : " + DBException);
        }
    }
    
    private void ambilKembalian(){
        
        if(txt_uang.getText().equals("")){
            JOptionPane.showMessageDialog(this, "Masukkan Uang Terlebih Dahulu");
        }
        else{
            int tot = Integer.parseInt(txt_total.getText());
            int uang = Integer.parseInt(txt_uang.getText());
            int kembalian = uang-total;
            
            txt_kembali.setText(""+kembalian);
        } 
    }
    
    public void bayarPesanan() {
        try {

            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, user, pass);
            Statement statement = connection.createStatement();

            String sql = "UPDATE isi_pesanan SET status='Dibayar' where id_pesanan='" + id_pesanan + "';";
            statement.executeUpdate(sql);
            
            String sql1 = "UPDATE meja SET status_meja='Kosong' where id_meja='" + id_meja + "';";
            statement.executeUpdate(sql1);

            statement.close();
            connection.close();
            
/*#################################################  BUAT PDF DENGAN ITEXT PDF  ##################################################*/
            URL location = BayarSemuaFrame.class.getProtectionDomain().getCodeSource().getLocation();
            
            Document document = new Document();
            String FILE = location.getFile() + "/bill_"+id_pesanan+".pdf";    
            PdfWriter.getInstance(document, new FileOutputStream(FILE));
            document.open();
            addContent(document,Integer.toString(id_pesanan),txt_uang.getText(),txt_kembali.getText());
            document.close();
/*#################################################  BUAT PDF DENGAN ITEXT PDF  ##################################################*/
            
        } catch (Exception DBException) {
            System.out.println("Error : " + DBException);
        }
    }
    
/*#################################################  BUAT ISI BILL PDF DENGAN ITEXT PDF  ##################################################*/    
    private static void addContent(Document document, String id_pesanan, String bayar, String kembali) throws DocumentException {
      Paragraph preface = new Paragraph();
      preface.add(new Paragraph("============================================================================\n",smallBold));
      addEmptyLine(preface, 1);
      preface.setTabSettings(new TabSettings(250f));
      preface.add(Chunk.TABBING);
      preface.add(new Chunk("---------SEDAP MALAM---------", catFont));
      addEmptyLine(preface, 1);
      preface.setTabSettings(new TabSettings(150f));
      preface.add(Chunk.TABBING);
      preface.add(new Chunk("__________Jalan Informatika No.8__________",smallBold));
      addEmptyLine(preface, 1);
      preface.add(new Paragraph("============================================================================\n",smallBold));
      addEmptyLine(preface, 1);
      try {
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, user, pass);
            Statement statement = connection.createStatement();
            String sql = "select a.id_pesanan,d.nama_meja,b.waktu,c.nama_makanan,c.harga_makanan,a.jumlah_makanan,a.status from isi_pesanan a, pesanan b, makanan c, meja d where b.id_pesanan='"+id_pesanan+"' and a.id_pesanan=b.id_pesanan and a.id_makanan=c.id_makanan and b.meja=d.id_meja";
            ResultSet rs = statement.executeQuery(sql);
            int i=0; int harga_tot=0;
            while (rs.next()){
                int id = rs.getInt(1);
                String nama_meja = rs.getString(2);
                String firstName = rs.getString(3);
                String surname = rs.getString(4);
                if(i==0){
                    
                    preface.add(new Chunk("ID Pesanan: "+rs.getInt(1)+"\n",normalFont));
                    preface.add(new Chunk("Meja: "+rs.getString(2)+"\n",normalFont));
                    preface.add(new Chunk("Waktu Pesanan: "+rs.getString(3),normalFont));
                    addEmptyLine(preface, 1);
                    preface.add(new Paragraph("---------------------------------------------------------------------------------------------------------------------------------\n",smallBold));
                    addEmptyLine(preface, 1);
                    preface.add(new Paragraph("Rincian Pesananan:",smallBold));
                    addEmptyLine(preface, 1);
                }
                preface.add(new Paragraph(rs.getString(4)+" @ "+rs.getString(6)+" = Rp. "+rs.getString(5)+"\n",normalFont));
                harga_tot+=Integer.parseInt(rs.getString(5))*Integer.parseInt(rs.getString(6));
                i++;
            }
            addEmptyLine(preface, 1);
            preface.add(new Paragraph("---------------------------------------------------------------------------------------------------------------------------------\n",smallBold));
            addEmptyLine(preface, 1);
            preface.add(new Paragraph("Harga Total: Rp. "+harga_tot,smallBold));
            addEmptyLine(preface, 1);
            preface.add(new Paragraph("Bayar: Rp. "+bayar,normalFont));
            addEmptyLine(preface, 1);
            preface.add(new Paragraph("Kembali: Rp. "+kembali,smallBold));
            addEmptyLine(preface, 3);
            preface.add(new Paragraph("============================== TERIMA KASIH ==============================",smallBold));
            
            rs.close();
            statement.close();
            connection.close();
      } 
      catch (Exception DBException) {}
  
      document.add(preface);
      document.newPage();
    }
    
/////////berguna untuk membuat enter kalau dalam word
    private static void addEmptyLine(Paragraph paragraph, int number) {
      for (int i = 0; i < number; i++) {
        paragraph.add(new Paragraph(" "));
      }
    }
/*#################################################  BUAT PDF DENGAN ITEXT PDF  ##################################################*/


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        txt_total = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txt_uang = new javax.swing.JTextField();
        txt_kembali = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        setTitle("Bayar");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "MENU", "Jumlah", "Harga"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        txt_total.setEditable(false);
        txt_total.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_totalActionPerformed(evt);
            }
        });

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("TOTAL :");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Jumlah Uang :");

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Kembalian :");

        txt_uang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cek_kembalianActionPerformed(evt);
            }
        });

        txt_kembali.setEditable(false);

        jButton1.setBackground(new java.awt.Color(255, 255, 153));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kafe/dolar.png"))); // NOI18N
        jButton1.setText("Bayar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(255, 255, 153));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kafe/refresh.png"))); // NOI18N
        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(255, 255, 153));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kafe/yes.png"))); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 204));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setText("Daftar Pesanan");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jMenu1.setText("File");

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText("Quit");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenu2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu2ActionPerformed(evt);
            }
        });

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("Makanan");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("Meja");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txt_uang))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txt_total, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButton2))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(txt_kembali)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButton3)))))))
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_total, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txt_uang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(txt_kembali, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
        new MejaFrame().setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenu2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenu2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
//        int tot = Integer.parseInt(txt_total.getText());
//        int uang = Integer.parseInt(txt_uang.getText());
        
        if(txt_uang.getText().equals("")){
            JOptionPane.showMessageDialog(this, "Masukkan Uang Terlebih Dahulu");
        }
        else if(Integer.parseInt(txt_uang.getText())<Integer.parseInt(txt_total.getText())){
            JOptionPane.showMessageDialog(this, "Uang Tidak Cukup");
        }
        else{
            if (JOptionPane.showConfirmDialog(this, "Apakah anda yakin ingin membayar semua pesanan?", "Bayar Semua",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                bayarPesanan();
                
                JOptionPane.showMessageDialog(this, "Pesanan Berhasil Dibayar");
                this.setVisible(false);
            }           
        } 
    }//GEN-LAST:event_jButton1ActionPerformed

    private void txt_totalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_totalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_totalActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        ambilKembalian();
    }//GEN-LAST:event_jButton3ActionPerformed

    
/*#####################################  FUNGSI WAKTU TEKAN ENTER UNTUK CEK KEMBALIAN  ######################################*/
    private void cek_kembalianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cek_kembalianActionPerformed
        // TODO add your handling code here:
        ambilKembalian();
    }//GEN-LAST:event_cek_kembalianActionPerformed
/*#####################################  FUNGSI TEKAN ENTER UNTUK CEK KEMBALIAN  ######################################*/

    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(BayarSemuaFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(BayarSemuaFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(BayarSemuaFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(BayarSemuaFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new BayarSemuaFrame().setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField txt_kembali;
    private javax.swing.JTextField txt_total;
    private javax.swing.JTextField txt_uang;
    // End of variables declaration//GEN-END:variables
}
