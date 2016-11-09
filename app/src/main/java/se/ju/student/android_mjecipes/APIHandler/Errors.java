package se.ju.student.android_mjecipes.APIHandler;

import java.net.HttpURLConnection;

public class Errors {
    public static final String ACCOUNT_USERNAME_MISSING = "UserNameMissing";
    public static final String ACCOUNT_INVALID_USERNAME = "InvalidUserName";
    public static final String ACCOUNT_DUPLICATE_USERNAME = "DuplicateUserName";
    public static final String ACCOUNT_PASSWORD_MISSING = "PasswordMissing";
    public static final String ACCOUNT_PASSWORD_TOO_SHORT = "PasswordTooShort";
    public static final String ACCOUNT_PASSWORD_REQUIRES_NON_ALPHANUM = "PasswordRequiresNonAlphanumeric";
    public static final String ACCOUNT_PASSWORD_REQUIRES_DIGIT = "PasswordRequiresDigit";
    public static final String ACCOUNT_PASSWORD_REQUIRES_LOWER = "PasswordRequiresLower";
    public static final String ACCOUNT_PASSWORD_REQUIRES_UPPER = "PasswordRequiresUpper";
    public static final String ACCOUNT_LATIDUTE_MISSING = "LongitudeMissing";
    public static final String ACCOUNT_LONGITUDE_MISSING = "LatitudeMissing";

    public static final String TOKEN_MISSING = "TokenMissing";
    public static final String TOKEN_INVALID = "TokenInvalid";

    public static final String RECIPE_ID_DOES_NOT_EXIST = "RecipeIdDoesNotExist";

    public static final String TOKEN_INVALID_REQUEST = "invalid_request";
    public static final String TOKEN_UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";
    public static final String TOKEN_INVALID_CLIENT = "invalid_client";

    public static final String RECIPE_NAME_MISSING = "NameMissing";
    public static final String RECIPE_NAME_WRONG_LENGTH = "NameWrongLength";
    public static final String RECIPE_DESCRIPTION_MISSING = "DescriptionMissing";
    public static final String RECIPE_DESCRIPTION_WRONG_LENGTH = "DescriptionWrongLength";
    public static final String RECIPE_DIRECTIONS_MISSING = "DirectionsMissing";
    public static final String RECIPE_DIRECTION_ORDER_MISSING = "DirectionOrderMissing";
    public static final String RECIPE_DIRECTION_DESCRIPTION_MISSING = "DirectionDescriptionMissing";
    public static final String RECIPE_DIRECTION_DESCRIPTION_WRONG_LENGTH = "DirectionDescriptionWrongLength";

    public static final String COMMENT_TEXT_MISSING = "TextMissing";
    public static final String COMMENT_TEXT_WRONG_LENGTH = "TextWrongLength";
    public static final String COMMENT_GRADE_MISSING = "GradeMissing";
    public static final String COMMENT_GRADE_WRONG_VALUE = "GradeWrongValue";
    public static final String COMMENT_COMMENTER_ID_MISSING = "CommenterIdMissing";
    public static final String COMMENT_COMMENTER_ALREDY_COMMENT = "CommenterAlreadyComment";

    public static final String SEARCH_TERM_MISSING = "TermMissing";

    public static final int HTTP_OK = HttpURLConnection.HTTP_OK;
    public static final int HTTP_CREATED = HttpURLConnection.HTTP_CREATED;
    public static final int HTTP_UNAUTHORIZED = HttpURLConnection.HTTP_UNAUTHORIZED;
    public static final int HTTP_NOT_FOUND = HttpURLConnection.HTTP_NOT_FOUND;
    public static final int HTTP_BAD_REQUEST = HttpURLConnection.HTTP_BAD_REQUEST;

    public String[] errors;
    public String error;
    public int HTTPCode;

    public boolean hasError(String error) {
        if(errors == null)
            return this.error.equals(error);

        for(String e: errors)
            if(e.equals(error))
                return true;
        return false;
    }
}
