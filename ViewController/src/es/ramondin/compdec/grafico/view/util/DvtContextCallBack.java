package es.ramondin.compdec.grafico.view.util;

import java.awt.Color;
import java.awt.Dimension;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.security.SecureRandom;

import java.util.GregorianCalendar;

import javax.faces.component.ContextCallback;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import oracle.adf.view.faces.bi.component.graph.Background;
import oracle.adf.view.faces.bi.component.graph.UIGraph;

import oracle.dss.dataView.ImageView;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.xalan.processor.TransformerFactoryImpl;


public class DvtContextCallBack implements ContextCallback {
    private UIGraph uiGraphCompDec;
    private Integer printWidth;
    private Integer printHeight;
    private String printRutaDestino;
    private String rutaFicheroXSL;
    private String textoEsquinaSupIzq;
    private String textoEsquinaSupDer;
    private String textoEsquinaInfIzq;
    private String textoEsquinaInfDer;
    private String ficheroGenerado;
    private Integer formatoSalida;

    public DvtContextCallBack() {
        super();
    }

    public DvtContextCallBack(UIGraph uiGraphCompDec, Integer printWidth, Integer printHeight, String printRutaDestino, String rutaFicheroXSL,
                              String textoEsquinaSupIzq, String textoEsquinaSupDer, String textoEsquinaInfIzq, String textoEsquinaInfDer, Integer formatoSalida) {
        super();

        this.uiGraphCompDec = uiGraphCompDec;
        this.printWidth = printWidth != null ? printWidth : 595;
        this.printHeight = printHeight != null ? printHeight : 841;
        this.printRutaDestino = printRutaDestino != null ? printRutaDestino.trim() : "c:/Temp/";

        this.printRutaDestino = this.printRutaDestino.replace("\\", "/");

        if (this.printRutaDestino.charAt(this.printRutaDestino.length() - 1) != '/')
            this.printRutaDestino += "/";

        this.rutaFicheroXSL = rutaFicheroXSL;

        this.textoEsquinaSupIzq = textoEsquinaSupIzq != null ? textoEsquinaSupIzq : "";
        this.textoEsquinaSupDer = textoEsquinaSupDer != null ? textoEsquinaSupDer : "";
        this.textoEsquinaInfIzq = textoEsquinaInfIzq != null ? textoEsquinaInfIzq : "";
        this.textoEsquinaInfDer = textoEsquinaInfDer != null ? textoEsquinaInfDer : "";

        this.ficheroGenerado = null;
        
        this.formatoSalida = formatoSalida;
    }

    public void invokeContextCallback(FacesContext facesContext, UIComponent uiComponent) {
        if (this.uiGraphCompDec != null)
            uiComponent = this.uiGraphCompDec;

        if (uiComponent instanceof UIGraph) {
            UIGraph dvtgraph = (UIGraph)uiComponent;

            Background orgBackground = dvtgraph.getBackground();
            Background bg = new Background();
            bg.setFillColor(Color.WHITE);
            bg.setFillTransparent(false);

            dvtgraph.setBackground(bg);
            dvtgraph.transferProperties();

            ImageView imgView = dvtgraph.getImageView();

            Dimension orgDimension = imgView.getImageSize();

            imgView.setImageSize(new Dimension(this.printWidth, this.printHeight));

            SecureRandom random = new SecureRandom();
            byte bytes[] = new byte[20];
            random.nextBytes(bytes);

            String fileName = this.printRutaDestino + GregorianCalendar.getInstance().getTimeInMillis() + "_" + random.nextInt();

            File file = null, filePDF = null;
            FileOutputStream fos, fosPDF;
            try {
                file = new File(fileName + ".png");
                fos = new FileOutputStream(file);
                imgView.exportToPNG(fos);
                fos.close();

                if (this.formatoSalida.equals(GraficoUtil.FORMATO_SALIDA_PDF)) {
                    //Exportamos a PDF
                    filePDF = new File(fileName + ".pdf");
                    fosPDF = new FileOutputStream(filePDF);
    
                    /* Exportación a PDF con iText (de pago)
                    Document document = new Document();
                    document.setPageSize(new Rectangle(this.printWidth, this.printHeight));
                    document.setMargins(0.0f, 0.0f, 0.0f, 0.0f);
                    PdfWriter.getInstance(document, fosPDF);
                    document.open();
    
                    Image imglogo = Image.getInstance(PngImage.getImage(file.getAbsolutePath()));
                    imglogo.scaleAbsolute(new Rectangle(this.printWidth, this.printHeight));
                    document.add(imglogo);
    
                    document.close();
                    */
    
                    /* Exportación a PDF con Apache FOP */
                    convertFO2PDF(fileName + ".png", true, filePDF);
    
                    fosPDF.close();
    
                    file.delete();
    
                    //Si todo ha ido bien
                    this.setFicheroGenerado(filePDF.getAbsolutePath());
                } else {
                    this.setFicheroGenerado(file.getAbsolutePath());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FOPException e) {
                e.printStackTrace();
            } finally {
                dvtgraph.setBackground(orgBackground);
                dvtgraph.transferProperties();
                imgView.setImageSize(orgDimension);
            }
        }
    }

    private void convertFO2PDF(String pngSource, boolean imprimirLogo, File filePDF) throws FOPException, IOException {
        OutputStream out = null;
        FopFactory fopFactory = FopFactory.newInstance();

        try {
            out = new BufferedOutputStream(new FileOutputStream(filePDF));

            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            Fop fop = fopFactory.newFop("application/pdf", foUserAgent, out);
            TransformerFactory factory = new TransformerFactoryImpl();
            Source src = new StreamSource(new File(this.rutaFicheroXSL));
            Transformer transformer = factory.newTransformer(src);
            Result res = new SAXResult(fop.getDefaultHandler());

            //Pasamos el parámetro con la ruta del fichero PNG
            transformer.setParameter("pngSource", pngSource);
            transformer.setParameter("imprimirLogo", imprimirLogo);
            transformer.setParameter("txtSupIzq", this.textoEsquinaSupIzq);
            transformer.setParameter("txtSupDer", this.textoEsquinaSupDer);
            transformer.setParameter("txtInfIzq", this.textoEsquinaInfIzq);
            transformer.setParameter("txtInfDer", this.textoEsquinaInfDer);
            transformer.transform(src, res);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }

    public void setFicheroGenerado(String ficheroGenerado) {
        this.ficheroGenerado = ficheroGenerado;
    }

    public String getFicheroGenerado() {
        return ficheroGenerado;
    }
}
