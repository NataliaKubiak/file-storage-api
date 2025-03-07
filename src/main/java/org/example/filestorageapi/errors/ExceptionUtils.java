package org.example.filestorageapi.errors;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.example.filestorageapi.security.CustomUserDetails;

@Log4j2
@UtilityClass
public class ExceptionUtils {

    public static void ifSessionExpiredThrowException(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new SessionExpiredException("Your session expired. Log in again.");
        }
    }
}
