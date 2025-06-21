package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.dto.EventAttendanceExportDTO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

public class EventAttendanceExcelExporter {
    public static Workbook export(List<EventAttendanceExportDTO> dtoList) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("참석자 명단");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("이름");
        headerRow.createCell(1).setCellValue("성별");
        headerRow.createCell(2).setCellValue("전화번호");
        headerRow.createCell(3).setCellValue("SNS 타입");
        headerRow.createCell(4).setCellValue("SNS 아이디");
        headerRow.createCell(5).setCellValue("참가비 납부 여부");
        headerRow.createCell(6).setCellValue("총 동행 인원");

        for (int i = 0; i < dtoList.size(); i++) {
            Row row = sheet.createRow(i + 1);
            EventAttendanceExportDTO dto = dtoList.get(i);
            row.createCell(0).setCellValue(nullToHyphen(dto.getName()));
            row.createCell(1).setCellValue(nullToHyphen(dto.getGender()));
            row.createCell(2).setCellValue(nullToHyphen(dto.getPhoneNumber()));
            row.createCell(3).setCellValue(nullToHyphen(dto.getSnsType()));
            row.createCell(4).setCellValue(nullToHyphen(dto.getSnsId()));
            row.createCell(5).setCellValue(dto.getIsPaid() != null ? (dto.getIsPaid() ? "예" : "아니오") : "-");
            row.createCell(6).setCellValue(dto.getTotalMember() != null ? dto.getTotalMember() : 0);
        }

        return workbook;
    }

    private static String nullToHyphen(String value) {
        return value != null ? value : "-";
    }
}