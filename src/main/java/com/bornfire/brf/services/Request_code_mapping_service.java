package com.bornfire.brf.services;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bornfire.brf.entities.Request_code_mapping_Entity;
import com.bornfire.brf.entities.Request_code_mapping_Repo;

@Service
public class Request_code_mapping_service {

	@Autowired
	Request_code_mapping_Repo request_code_mapping_repo;

	public String add(Request_code_mapping_Entity Request_code_mapping_Entity) {

		request_code_mapping_repo.save(Request_code_mapping_Entity);

		return "addsuccessfully";

	}

	public byte[] generateExcel(String keyword) {
		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Marks Report");

			/*
			CellStyle headerStyle = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setBold(true);
			headerStyle.setFont(font);
			headerStyle.setAlignment(HorizontalAlignment.CENTER);
			headerStyle.setBorderTop(BorderStyle.THIN);
			headerStyle.setBorderBottom(BorderStyle.THIN);
			headerStyle.setBorderLeft(BorderStyle.THIN);
			headerStyle.setBorderRight(BorderStyle.THIN);
			*/
			// Common border style
			BorderStyle border = BorderStyle.THIN;

			// Header style (left aligned)
			CellStyle headerStyle = workbook.createCellStyle();
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 10);
			headerStyle.setFont(headerFont);
			headerStyle.setAlignment(HorizontalAlignment.LEFT);
			headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerStyle.setBorderTop(border);
			headerStyle.setBorderBottom(border);
			headerStyle.setBorderLeft(border);
			headerStyle.setBorderRight(border);
			// Default data style (left aligned)
			CellStyle dataStyle = workbook.createCellStyle();
			dataStyle.setAlignment(HorizontalAlignment.LEFT);
			dataStyle.setBorderTop(border);
			dataStyle.setBorderBottom(border);
			dataStyle.setBorderLeft(border);
			dataStyle.setBorderRight(border);


			Row row2 = sheet.createRow(0);
			String[] headers = { "Customer Id", "Account No", "Name of Account", "Report Code", "Report Label",
					"Scheme Code", "GLSH", "Criteria1", "Criteria2", "Criteria3" };
			for (int i = 0; i < headers.length; i++) {
				Cell cell = row2.createCell(i);
				cell.setCellValue(headers[i]);
				cell.setCellStyle(headerStyle);
			}
			List<Request_code_mapping_Entity> list = new ArrayList<>();

			if (keyword.equals("List")) {
				list = request_code_mapping_repo.findAll();
			}

			else if (keyword.equals("MAList")) {
				list = request_code_mapping_repo.getListbyMAList();
			} else if (keyword.equals("UMAList")) {
				list = request_code_mapping_repo.getListbyUMAList();
			}

			int rowNum = 1;
			for (Request_code_mapping_Entity m : list) {
				Row dataRow = sheet.createRow(rowNum++);
				
				/*
				dataRow.createCell(0).setCellValue(m.getCustomer_Id()); 
				dataRow.createCell(1).setCellValue(m.getAccount_No());
				dataRow.createCell(2).setCellValue(m.getAccount_Name());
				dataRow.createCell(3).setCellValue(m.getReport_Code());
				dataRow.createCell(4).setCellValue(m.getReport_Label());
				dataRow.createCell(5).setCellValue(m.getScheme_Code());
				dataRow.createCell(6).setCellValue(m.getGLSH());
				dataRow.createCell(7).setCellValue(m.getCriteria1());
				dataRow.createCell(8).setCellValue(m.getCriteria2());
				dataRow.createCell(9).setCellValue(m.getCriteria3());
				*/
				
				Cell cell1 = dataRow.createCell(0);
				cell1.setCellValue(m.getCustomer_Id());
				cell1.setCellStyle(dataStyle);

				Cell cell2 = dataRow.createCell(1);
				cell2.setCellValue(m.getAccount_No());
				cell2.setCellStyle(dataStyle);

				Cell cell3 = dataRow.createCell(2);
				cell3.setCellValue(m.getAccount_Name());
				cell3.setCellStyle(dataStyle);
				
				Cell cell4 = dataRow.createCell(3);
				cell4.setCellValue(m.getReport_Code());
				cell4.setCellStyle(dataStyle);
				
				Cell cell5 = dataRow.createCell(4);
				cell5.setCellValue(m.getReport_Label());
				cell5.setCellStyle(dataStyle);
				
				Cell cell6 = dataRow.createCell(5);
				cell6.setCellValue(m.getScheme_Code());
				cell6.setCellStyle(dataStyle);
				
				Cell cell7 = dataRow.createCell(6);
				cell7.setCellValue(m.getGLSH());
				cell7.setCellStyle(dataStyle);
				
				Cell cell8 = dataRow.createCell(7);
				cell8.setCellValue(m.getCriteria1());
				cell8.setCellStyle(dataStyle);
				
				Cell cell9 = dataRow.createCell(8);
				cell9.setCellValue(m.getCriteria2());
				cell9.setCellStyle(dataStyle);
				
				Cell cell10 = dataRow.createCell(9);
				cell10.setCellValue(m.getCriteria3());
				cell10.setCellStyle(dataStyle);
				


			}

			for (int i = 0; i <= 4; i++)
				sheet.autoSizeColumn(i);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workbook.write(out);
			return out.toByteArray();

		} catch (Exception e) {
			throw new RuntimeException("Error generating Excel", e);
		}
	}

}
