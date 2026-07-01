package com.moneyfi.constants.constants;

import com.moneyfi.constants.enums.ReasonEnum;
import com.moneyfi.constants.enums.UserRoles;
import org.apache.poi.ss.usermodel.CellStyle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.moneyfi.constants.enums.ReasonEnum.*;
import static com.moneyfi.constants.enums.ReasonEnum.ADMIN_RETRIEVAL;

public class CommonConstants {

    private CommonConstants() {}

    public static final String MONEYFI_APPLICATION_NAME = "MoneyFi";

    public static final String DATE_TIME_UNDERSCORE_PATTERN = "yyyy_MM_dd HH_mm_ss";
    public static final String DATE_TIME_GENERIC_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final Map<Integer, String> userRoleAssociation = Map.of(1, UserRoles.ADMIN.name(), 2, UserRoles.USER.name(), 3, UserRoles.DEVELOPER.name(), 4, UserRoles.MAINTAINER.name());
    public static final Map<ReasonEnum, Integer> reasonCodeIdAssociation =
            Map.ofEntries(
                    Map.entry(BLOCK_ACCOUNT, 1),
                    Map.entry(PASSWORD_CHANGE, 2),
                    Map.entry(NAME_CHANGE, 3),
                    Map.entry(UNBLOCK_ACCOUNT, 4),
                    Map.entry(DELETE_ACCOUNT, 5),
                    Map.entry(ACCOUNT_RETRIEVAL, 6),
                    Map.entry(PHONE_NUMBER_CHANGE, 7),
                    Map.entry(FORGOT_PASSWORD, 8),
                    Map.entry(USER_RAISED_REQUEST_IGNORED, 9),
                    Map.entry(FORGOT_USERNAME, 10),
                    Map.entry(ADMIN_CREATION, 11),
                    Map.entry(ADMIN_UPDATE, 12),
                    Map.entry(ADMIN_BLOCK, 13),
                    Map.entry(ADMIN_UNBLOCK, 14),
                    Map.entry(ADMIN_DELETE, 15),
                    Map.entry(ADMIN_RETRIEVAL, 16)
            );
    public static final Map<String, CellStyle> dateStyles = new HashMap<>();

    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static final String OFFSET = "offset";
    public static final String LIMIT = "limit";
    public static final String SEARCH = "search";
    public static final String SEARCH_BY = "searchBy";
    public static final String SORT_BY = "sortBy";
    public static final String SORT_ORDER = "sortOrder";
    public static final String STATUS = "status";
    public static final String EXCEL_FORMAT = "xlsx";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer";
    public static final String ROLES = "roles";
    public static final String ROLE_ = "ROLE_";
    public static final String TOKEN_BLACKLISTED_MESSAGE = "Token is Blacklisted";
    public static final String USER_VALIDATION_FAILED_MESSAGE = "Failed to validate the user";
    public static final String USER_ID = "userId";
    public static final String REDIS_BLACKLIST_TOKEN_PREFIX_KEY = "blackListToken";

    public static final String DOT = ".";
    public static final String SPACE = " ";
    public static final String DOUBLE_COLON = "::";
    public static final String COLON = ":";


    public static String generateVerificationCode() {
        Random random = new Random();
        int verificationCode = 100000 + random.nextInt(900000);
        return String.valueOf(verificationCode);
    }

    public static String generateAlphabetCode() {
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            int index = random.nextInt(ALPHABET.length());
            code.append(ALPHABET.charAt(index));
        }
        return code.toString();
    }

    public static Path prepareOutputPath(String fileName, String outputDirectory) throws IOException {
        Path dir = Paths.get(outputDirectory).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        return dir.resolve(fileName);
    }

    public static void deleteLocalFile(Path localFilePath) throws IOException {
        if (Files.exists(localFilePath)) Files.deleteIfExists(localFilePath);
    }

    public static String functionToGenerateFileNameForReports(String prefix, LocalDateTime time) {
        return prefix + SPACE + time.format(DateTimeFormatter.ofPattern(DATE_TIME_UNDERSCORE_PATTERN)) + DOT + EXCEL_FORMAT;
    }
}
