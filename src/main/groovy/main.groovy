package io.endeios

import static groovyx.javafx.GroovyFX.start
import javafx.scene.control.*
import javafx.fxml.FXMLLoader
import groovy.transform.Canonical

import java.awt.print.*;
//import javafx.print.* //javafx print is the same as awt anyway... also pdfbox has not support till today
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageable

import business.domain.*
import business.BusinessApp


@Canonical
class MyTab{
	String title
	String label
	List<String> cols
	def crud_service;
}

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
				//PrinterJob pj = PrinterJob.createPrinterJob();
				boolean doPrint = pj.printDialog();
				//boolean doPrint = pj.showPrintDialog(stage)
				if(doPrint){
                  InputStream input = new FileInputStream(new File("/home/bveronesi/reportBusiness.jrxml"));
                  JasperDesign design = JRXmlLoader.load(input);
                  JasperReport report = JasperCompileManager.compileReport(design);
                  def data = new JRBeanCollectionDataSource([test.invoice])
                  println data
                  JasperPrint myPrint = JasperFillManager.fillReport(report,[:] ,data)

				  println pj					
				  PDDocument pdf = PDDocument.load("/home/bruno/Scaricati/pragmatic-guide-to-sass_p1_0.pdf")
				  pj.setPageable(new PDPageable(pdf, pj))
				  pj.setJobName("InvoicesXXXXXXX");
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


