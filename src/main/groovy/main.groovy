package io.endeios

import static groovyx.javafx.GroovyFX.start
import javafx.scene.control.*
import javafx.fxml.FXMLLoader
import groovy.transform.Canonical

import java.awt.print.*;
//import javafx.print.* //javafx print is the same as awt anyway... also pdfbox has not support till today
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageable

import net.sf.jasperreports.engine.*
import net.sf.jasperreports.engine.design.JasperDesign
import net.sf.jasperreports.engine.xml.JRXmlLoader
import net.sf.jasperreports.engine.export.*
import net.sf.jasperreports.engine.data.*


import business.domain.*
import business.BusinessApp


@Canonical
class MyTab{
	String title
	String label
	List<String> cols
	def crud_service;
}

def cl = Thread.currentThread().getContextClassLoader()

def tabs = [
	new MyTab(title:"Customer",label:"Customer's data",cols:["name","email"],crud_service:business.BusinessApp.getCustomerService()),
	new MyTab(title:"Items",label:"Item's data",cols:["name","price"],crud_service:business.BusinessApp.getItemService()),
	new MyTab(title:"Invoice",label:"Invoice's data",cols:["date","items","customer"],crud_service:business.BusinessApp.getInvoiceService())
	]

start{
    stage(title:"Welcome",visible:true,id:"stage"){
        scene(fill:BLACK, width:500,height:300,id:"scene"){
            borderPane{
                top{
                    menuBar{
                        menu("File"){
                            menuItem("Print Invoices",onAction:{evt->
                                PrinterJob pj = PrinterJob.getPrinterJob();
                                //PrinterJob pj = PrinterJob.createPrinterJob();//JavaFx
                                boolean doPrint = pj.printDialog();
                                //boolean doPrint = pj.showPrintDialog(stage)//JavaFx
                                if(doPrint){
                                    InputStream input = cl.getResourceAsStream("reportBusiness.jrxml")
                                    JasperDesign design = JRXmlLoader.load(input);
                                    JasperReport report = JasperCompileManager.compileReport(design);
                                    def data = new JRBeanCollectionDataSource(business.BusinessApp.getInvoiceService().findAll())
                                    println data
                                    JasperPrint myPrint = JasperFillManager.fillReport(report,[:] ,data)
                                    def output = new ByteArrayOutputStream()
                                    
                                    JasperExportManager.exportReportToPdfStream(myPrint, output);
                                    def pdInput = new ByteArrayInputStream(output.toByteArray())
                                    println pj
                                    println output
                                    PDDocument pdf = PDDocument.load(pdInput)

                                    pj.setPageable(new PDPageable(pdf, pj))
                                    pj.setJobName("Invoices");
                                    try {
                                        pj.print();
                                    } catch (PrinterException e) {
                                        System.out.println(e);
                                    }
                              }
                            })
                        }
                    }
                }
                center{
                    tabPane(){
                        tabs.each{aTab ->
                            tab(aTab.title,closable:false){
                                label(aTab.label)
                                tableView(selectionMode: "single", cellSelectionEnabled: true, editable: true, items: aTab.crud_service.findAll()){
                                    aTab.cols.each{colName->
                                        tableColumn(editable:true,property:colName,text:colName,
                                        onEditCommit: { event ->
                                            def item = event.tableView.items.get(event.tablePosition.row)
                                            item[colName] = event.newValue;
                                        aTab.crud_service.save(item)
                                        //event.tableView.items = aTab.service.findAll()
                                        }
                                        )
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}


