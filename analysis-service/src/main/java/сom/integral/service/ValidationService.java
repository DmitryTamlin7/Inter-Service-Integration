package сom.integral.service;

import org.springframework.stereotype.Service;
import сom.integral.domain.ValidationReport;

import java.util.List;

@Service
public class ValidationService {

    private static final long MAX_FILE_SIZE = 1024 * 1024;
    private static final List<String> ALLOWED_EXTENSIONS = List.of("pdf", "docx", "txt");

    public ValidationReport validate(String fileName, long fileSize){
        ValidationReport report = new ValidationReport();

        if (fileSize > MAX_FILE_SIZE){
            report.addRemark("Размер файла превышает лимит в 1 МБ. \n Текущий размер " + fileSize + " Байт.");
        }

        String extension =  getFileExtension(fileName).toLowerCase();

        if (extension.equals("zip")){
            report.addRemark("Загрузка Архивов (ZIP) запрещена. Ваш файл имеет имя" + fileName  + " Разрешены только: pdf, docx, txt.");
        } else if (!ALLOWED_EXTENSIONS.contains(extension)) {
            report.addRemark("Недопустимый формат файла: " + extension + " Разрешены только: pdf, docx, txt.");
        }

        if (report.isAccepted()){
            report.setStatus("ПРИНЯТО");
        }
        else {
            report.setStatus("ТРЕБУЕТСЯ ДОРАБОТКА");
        }
        return report;
    }

    private String getFileExtension(String fileName){
        if (fileName == null || !fileName.contains(".")){
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".")+ 1);
    }
}
