package ui.common;

import java.sql.SQLException;
import java.util.function.Consumer;

public final class UiExceptionHandler {

  private static final String DATABASE_ERROR_MESSAGE =
      "데이터베이스 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";

  private UiExceptionHandler() {
  }

  public static void run(Consumer<String> logger, UiAction action) {
    try {
      action.run();
    } catch (Exception e) {
      log(logger, e);
    }
  }

  public static void log(Consumer<String> logger, Exception exception) {
    String message = messageFor(exception);
    logger.accept("오류: " + message);
    javax.swing.JOptionPane.showMessageDialog(null, message, "오류", javax.swing.JOptionPane.ERROR_MESSAGE);
  }

  public static String messageFor(Throwable throwable) {
    if (containsSqlException(throwable)) {
      return DATABASE_ERROR_MESSAGE;
    }

    String message = throwable.getMessage();
    if (message == null || message.isBlank()) {
      return "처리 중 오류가 발생했습니다.";
    }
    return message;
  }

  private static boolean containsSqlException(Throwable throwable) {
    Throwable current = throwable;
    while (current != null) {
      if (current instanceof SQLException) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }

  @FunctionalInterface
  public interface UiAction {

    void run() throws Exception;
  }
}
