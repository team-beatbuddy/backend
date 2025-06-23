package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.dto.EventAttendanceExportDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

public class EventAttendanceExcelExporter {
    public static Workbook export(List<EventAttendanceExportDTO> dtoList) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("참석자 명단");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("신청일");
        headerRow.createCell(1).setCellValue("이름");
        headerRow.createCell(2).setCellValue("성별");
        headerRow.createCell(3).setCellValue("전화번호");
        headerRow.createCell(4).setCellValue("SNS 타입");
        headerRow.createCell(5).setCellValue("SNS 아이디");
        headerRow.createCell(6).setCellValue("참가비 납부 여부");
        headerRow.createCell(7).setCellValue("총 동행 인원");

        for (int i = 0; i < dtoList.size(); i++) {
            Row row = sheet.createRow(i + 1);
            EventAttendanceExportDTO dto = dtoList.get(i);
            Cell dateCell = row.createCell(0);
            if (dto.getLocalDateTime() != null) {
                CellStyle dateStyle = workbook.createCellStyle();
                CreationHelper createHelper = workbook.getCreationHelper();
                dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm"));
                dateCell.setCellStyle(dateStyle);
                dateCell.setCellValue(java.sql.Timestamp.valueOf(dto.getLocalDateTime()));
            } else {
                dateCell.setCellValue("-");
            }

            row.createCell(1).setCellValue(nullToHyphen(dto.getName()));
            row.createCell(2).setCellValue(nullToHyphen(dto.getGender()));
            row.createCell(3).setCellValue(nullToHyphen(dto.getPhoneNumber()));
            row.createCell(4).setCellValue(nullToHyphen(dto.getSnsType()));
            row.createCell(5).setCellValue(nullToHyphen(dto.getSnsId()));

            Cell paidCell = row.createCell(6, CellType.STRING);
            paidCell.setCellValue(
                    "-".equals(dto.getIsPaid())
                            ? "-"
                            : ("true".equals(dto.getIsPaid()) ? "예" : "아니오")
            );

            row.createCell(7).setCellValue(nullToHyphen(dto.getTotalMember()));
        }

        return workbook;
    }

    private static String nullToHyphen(String value) {
        return value != null ? value : "-";
    }
}