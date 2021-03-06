/*
 * SIProjektView.java
 */

package siprojekt;

import java.awt.Font;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import weka.core.Instances; 
import weka.core.converters.ConverterUtils.DataSource;
import java.io.BufferedReader; 
import java.io.FileReader; 
import javax.swing.table.TableColumn;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Debug.Random;
import weka.core.Instance;
//import weka.core.converters.ConverterUtils.DataSource;


public class SIProjektView extends FrameView {
    File[] files;
    String[] filesstr;
    Instances data;
    Attribute atrybuty[];
    Vector listagrup;
    javax.swing.table.DefaultTableModel atrybutyTableModel;
    double[][] odleglosci;

    public SIProjektView(SingleFrameApplication app) {
        
        super(app);
        int k;
        atrybutyTableModel = new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {new Boolean(true), new String("atrybut 1"), new Double(1.0)},
                {new Boolean(true), new String("atrybut 2"), new Double(1.0)},
                {new Boolean(false), new String("atrybut 3"), new Double(1.0)},
                {new Boolean(true), new String("atrybut 4"), new Double(1.0)},
                {new Boolean(false), new String("atrybut 5"), new Double(1.0)}
            },
            new String [] {
                "", "atrybut", "waga"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.String.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        };

        initComponents();
        
        Font font = new Font("Monospace", Font.PLAIN, 12);
        jTextArea1.setFont(font);
        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                }
            }
        });
        //blok wczytujący na starcie listę plików katalogu data
        
        String currentDirectory = new String("./data/");//(System.getProperty("user.dir"));
        //System.out.println(currentDirectory);
        jList1.removeAll();
        File dir = new File(currentDirectory);
        FileFilter fileFilter = new FileFilter()
        {
            public boolean accept(File file)
            {
                    return file.isFile();
            }
        };
        files = dir.listFiles(fileFilter);
       
        filesstr=new String[files.length];
        for (k=0;k<files.length;k++)
        {
            filesstr[k]=new String(files[k].getName());
        }
        
        jList1.setListData(filesstr);
        
        //TODO: ustawić <max> na ilość wczytanych pozycji z pliku arffdata.numInstances() 
        //iloscGrupjSpinner.setModel(new javax.swing.SpinnerNumberModel(3, 1, <max>, 1));
        //atrybutyjTable.setModel(atrybutyTableModel);
        atrybutyjTable.getColumnModel().getColumn(0).setMinWidth(20);
        atrybutyjTable.getColumnModel().getColumn(0).setMaxWidth(20);
        atrybutyjTable.getColumnModel().getColumn(2).setMinWidth(40);
        atrybutyjTable.getColumnModel().getColumn(2).setMaxWidth(40);
        wykasujTabeleAtrybuty();
        setEnabled(false);
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = SIProjektApp.getApplication().getMainFrame();
            aboutBox = new SIProjektAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        SIProjektApp.getApplication().show(aboutBox);
    }
    
    private void setEnabled(Boolean e){
        atrybutyjScrollPane.setEnabled(e);
        atrybutyjTable.setEnabled(e);
        metrykajSpinner.setEnabled(e);
        strategiajComboBox.setEnabled(e);
        iloscGrupjSpinner.setEnabled(e);
        jButton3.setEnabled(e);
    }
    
    public void customWczytajKatalog()
    {
        int k;
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String currentDirectory = new String(System.getProperty("user.dir"));
        int returnVal = chooser.showOpenDialog(jList1);
        if (returnVal == JFileChooser.APPROVE_OPTION){
            currentDirectory = chooser.getSelectedFile().getPath();
        }
        jList1.removeAll();
        File dir = new File(currentDirectory);
        FileFilter fileFilter = new FileFilter()
        {
            public boolean accept(File file)
            {
                    return file.isFile();
            }
        };
        files = dir.listFiles(fileFilter);
        //do zrobienia - przerobienie files na listę stringów
        filesstr=new String[files.length];
        for (k=0;k<files.length;k++)
        {
            filesstr[k]=new String(files[k].getName());
        }
        
        jList1.setListData(filesstr);
        wykasujTabeleAtrybuty();
        setEnabled(false);
    }
    
    private void customWyświetlGrupy()
    {
        int k;
        jTextArea1.setText("");
        for (k=0;k<listagrup.size();k++)
        {
            
            jTextArea1.append("\n"+"Grupa: "+(k+1)+"\n");
            jTextArea1.append((listagrup.get(k)).toString());
        }
        
        
    }
    
    private void wykasujTabeleAtrybuty(){
        int rc=atrybutyTableModel.getRowCount();
        for(int i = 0; i < rc; i++)
            atrybutyTableModel.removeRow(0);
    }
    
    public void customWybierzAtrybuty() throws Exception
    {
        int i;
        wykasujTabeleAtrybuty();
        
        atrybuty = new Attribute[data.numAttributes()-1];
        for (i=0;i<data.numAttributes()-1;i++){
            atrybuty[i]=data.attribute(i);
            atrybutyTableModel.addRow(new Object[]{new Boolean(true), data.attribute(i).name(), new Double(1.0)});
        }
        
    }
    
    public void customObliczOdleglosci()
    {
        int k=0,w=0,i=0,ix,iy;
        Instance tmp1,tmp2;
        double odlegloscitmp[][];
        double max,min,aver;
        //dla listygrup obliczanie odległości między nimi
        
        odleglosci=new double[listagrup.size()][listagrup.size()];
        int metryka = Integer.parseInt(metrykajSpinner.getValue().toString());
        
        //do kwadratowj tablicy wpisujemy odległości między elementami
        for (k=0;k<listagrup.size();k++)
            for (w=0;w<listagrup.size();w++)
            {
                odleglosci[k][w]=0;
                int x=((Vector)listagrup.get(k)).size(),y=((Vector)listagrup.get(w)).size();
                odlegloscitmp=new double[x][y];
                if (k!=w) 
                {
                    //obliczanie wszystkich odleglosci miedzy wszystkimi obiektami, po czym zapsianie tylko jednej do glownej tablicy w zaleznosci od strategii
                    for (ix=0;ix<x;ix++)
                        for (iy=0;iy<y;iy++)
                        {
                            for (i=0;i<atrybutyjTable.getRowCount();i++)
                    {
                        tmp1=(Instance)((Vector)listagrup.get(k)).get(ix);
                        tmp2=(Instance)((Vector)listagrup.get(w)).get(iy);
                        
                        if (atrybutyjTable.getValueAt(i, 0).toString()=="true") odlegloscitmp[ix][iy]+=Math.pow(tmp1.value(i)-tmp2.value(i),metryka);
                        //System.out.println("costam: "+odlegloscitmp[ix][iy]);
                    }
                    //w linii niżej zamienia wszystkie odległości na 1.0 
                    //odlegloscitmp[ix][iy]=Math.pow(odlegloscitmp[ix][iy],(1/metryka));
                            //System.out.println("metryka"+metryka+"cudo: "+odlegloscitmp[ix][iy]);
                        }
                    //szukanie max min i average w tablicy odleglosci miedzy dwoma grupami
                    max=0;
                    min=99999;
                    aver=0;
                    for (ix=0;ix<x;ix++)
                        for (iy=0;iy<y;iy++)
                        {
                            if (odlegloscitmp[ix][iy]>max) max=odlegloscitmp[ix][iy];
                            if (odlegloscitmp[ix][iy]<min) min=odlegloscitmp[ix][iy];
                        }
                    for (ix=0;ix<x;ix++)
                        for (iy=0;iy<y;iy++)
                            if (odlegloscitmp[ix][iy]<aver+min-max) aver=odlegloscitmp[ix][iy];
                    //System.out.println("min: "+min+" max: "+max+" aver: "+aver);
                    if (strategiajComboBox.getSelectedIndex()==0)
                    {
                        odleglosci[k][w]=min;
                        //System.out.println("nearest");
                    }
                            
                    if (strategiajComboBox.getSelectedIndex()==1)
                    {
                        //System.out.println("average");
                        odleglosci[k][w]=aver;
                    }
                            
                    if (strategiajComboBox.getSelectedIndex()==2)
                    {
                        odleglosci[k][w]=max;
                        //System.out.println("farest");
                    }
                            
                }
            }
        
        
    }
    
    public void customGrupujHierarchicznie()
    {
        //grupowanie działa dla nearest first, analogicznie musi wyszukiwać maksimum i average
        int k,w,mink,minw,maxk,maxw,averk,averw;
        double min=99999,max=0,aver=0;
        while (listagrup.size()>Integer.parseInt(
            iloscGrupjSpinner.getValue().toString()))
        {
            
            
            //szukanie minimalnej odleglosci
            if (strategiajComboBox.getSelectedIndex()==0)
            {
                customObliczOdleglosci();
                mink=999999;
            minw=999999;
            min=999999;
            for (k=0;k<listagrup.size();k++)
                for (w=k+1;w<listagrup.size();w++)
                {
                    //System.out.println(odleglosci[k][w]);
                    if (odleglosci[k][w]<min && odleglosci[k][w]!=0) 
                    {
                        min=odleglosci[k][w];
                        mink=k;
                        minw=w;
                    }
                }
            System.out.println(mink+" "+minw+" "+min);
            //po znalezieniu minimalnej odległości łączymy te dwie grupy
            if (mink!=999999) 
            {
                ((Vector)listagrup.get(mink)).addAll((Vector)listagrup.get(minw));
                listagrup.remove(minw);
            }
            }
            
            //szukanie maksymalnej odległości
            if (strategiajComboBox.getSelectedIndex()==2)
            {
                customObliczOdleglosci();
                maxk=999999;
            maxw=999999;
            max=0;
            for (k=0;k<listagrup.size();k++)
                for (w=k+1;w<listagrup.size();w++)
                {
                    //System.out.println(odleglosci[k][w]);
                    if (odleglosci[k][w]>max && odleglosci[k][w]!=0) 
                    {
                        max=odleglosci[k][w];
                        maxk=k;
                        maxw=w;
                    }
                }
            System.out.println(maxk+" "+maxw+" "+max);
            //po znalezieniu minimalnej odległości łączymy te dwie grupy
            if (maxk!=999999) 
            {
                ((Vector)listagrup.get(maxk)).addAll((Vector)listagrup.get(maxw));
                listagrup.remove(maxw);
            }
            }
            
            //szukanie average
            if (strategiajComboBox.getSelectedIndex()==1)
            {
                customObliczOdleglosci();
                averk=999999;
            averw=999999;
            aver=0;
            for (k=0;k<listagrup.size();k++)
                for (w=k+1;w<listagrup.size();w++)
                {
                    //System.out.println(odleglosci[k][w]);
                    if (odleglosci[k][w]>aver-3 && odleglosci[k][w]!=0) 
                    {
                        aver=odleglosci[k][w];
                        averk=k;
                        averw=w;
                    }
                }
            System.out.println(averk+" "+averw+" "+aver);
            //po znalezieniu minimalnej odległości łączymy te dwie grupy
            if (averk!=999999) 
            {
                ((Vector)listagrup.get(averk)).addAll((Vector)listagrup.get(averw));
                listagrup.remove(averw);
            }
            }
        }
    }
    
    public void customTworzListeGrup()
    {
        int k;
        Vector tmp;
        listagrup=new Vector(data.numInstances());//utworzona lista grup
        //teraz w każde pole wkładamy jednoelementowy wektor;
        for (k=0;k<data.numInstances();k++) 
        {
            tmp=new Vector();
            tmp.add(data.instance(k));
            listagrup.add(tmp);
        }
    }
    
    public void customWczytajPlik() throws Exception
    {
        DataSource source;
        
        try {
            source = new DataSource(files[jList1.getSelectedIndex()].getAbsolutePath());
            data = source.getDataSet();
            if (data.classIndex() == -1)
                data.setClassIndex(data.numAttributes() - 1);
                
                jTextArea1.setText(data.toString());
                //jTextArea1.setText(files[jList1.getSelectedIndex()].getAbsolutePath());
                
                iloscGrupjSpinner.setModel(new javax.swing.SpinnerNumberModel(3, 1, data.numInstances(), 1));
        } catch (Exception ex) {
            Logger.getLogger(SIProjektView.class.getName()).log(Level.SEVERE, null, ex);
        }

        customWybierzAtrybuty();
        customTworzListeGrup();
        
        setEnabled(true);

        //odczyt pliku do Stringa i potem wczytanie go do jtextarea
        /*StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(files[jList1.getSelectedIndex()]));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SIProjektView.class.getName()).log(Level.SEVERE, null, ex);
        }
        char[] buf = new char[1024];
        int numRead=0;
        try {
            while((numRead=reader.read(buf)) != -1){
                fileData.append(buf, 0, numRead);
            }
        } catch (IOException ex) {
            Logger.getLogger(SIProjektView.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(SIProjektView.class.getName()).log(Level.SEVERE, null, ex);
        }
        jTextArea1.setText(fileData.toString());*/
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        atrybutyjScrollPane = new javax.swing.JScrollPane();
        atrybutyjTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        strategiajComboBox = new javax.swing.JComboBox();
        iloscGrupjSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        metrykajSpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();

        mainPanel.setMinimumSize(new java.awt.Dimension(640, 480));
        mainPanel.setName("mainPanel"); // NOI18N

        jSplitPane1.setBorder(null);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jPanel1.setMinimumSize(new java.awt.Dimension(200, 400));
        jPanel1.setName("jPanel1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(siprojekt.SIProjektApp.class).getContext().getResourceMap(SIProjektView.class);
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.setName("jList1"); // NOI18N
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jList1);

        atrybutyjScrollPane.setMinimumSize(new java.awt.Dimension(100, 107));
        atrybutyjScrollPane.setName("atrybutyjScrollPane"); // NOI18N

        atrybutyjTable.setModel(atrybutyTableModel);
        atrybutyjTable.setName("atrybutyjTable"); // NOI18N
        atrybutyjScrollPane.setViewportView(atrybutyjTable);
        atrybutyjTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("atrybutyjTable.columnModel.title0")); // NOI18N
        atrybutyjTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("atrybutyjTable.columnModel.title1")); // NOI18N
        atrybutyjTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("atrybutyjTable.columnModel.title2")); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        strategiajComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "nearest first", "average first", "farest first"}));
        strategiajComboBox.setName("strategiajComboBox"); // NOI18N

        iloscGrupjSpinner.setModel(new javax.swing.SpinnerNumberModel(3, 1, 10, 1));
        iloscGrupjSpinner.setName("iloscGrupjSpinner"); // NOI18N
        iloscGrupjSpinner.setValue(3);

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton3MouseClicked(evt);
            }
        });

        metrykajSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(2), Integer.valueOf(1), null, Integer.valueOf(1)));
        metrykajSpinner.setName("metrykajSpinner"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(atrybutyjScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .addComponent(strategiajComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 180, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(iloscGrupjSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(metrykajSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(atrybutyjScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(metrykajSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(5, 5, 5)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(strategiajComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(iloscGrupjSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel1);

        jPanel2.setName("jPanel2"); // NOI18N

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText(resourceMap.getString("jTextArea1.text")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setName("jTextArea1"); // NOI18N
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 566, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 461, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jSplitPane1.setRightComponent(jPanel2);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(siprojekt.SIProjektApp.class).getContext().getActionMap(SIProjektView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 751, Short.MAX_VALUE)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
        // TODO add your handling code here:
        customWczytajKatalog();
    }//GEN-LAST:event_jButton2MouseClicked

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
        customGrupujHierarchicznie();
        customWyświetlGrupy();
    }//GEN-LAST:event_jButton3MouseClicked

    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
        try {
            customWczytajPlik();
        } catch (Exception ex) {
            Logger.getLogger(SIProjektView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jList1ValueChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane atrybutyjScrollPane;
    private javax.swing.JTable atrybutyjTable;
    private javax.swing.JSpinner iloscGrupjSpinner;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JSpinner metrykajSpinner;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JComboBox strategiajComboBox;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
