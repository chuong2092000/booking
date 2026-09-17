package project.notificationservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import project.commondto.dto.hotel.internal.InternalFullRoomTypeDto;
import project.commondto.dto.hotel.internal.InternalRoomInventoryDto;
import project.commondto.dto.notification.MailBookingCanceled;
import project.commondto.dto.notification.MailBookingSuccess;
import project.commondto.dto.notification.MailPaymentRefunding;
import project.commondto.dto.notification.MailPaymentSuccess;
import project.commondto.dto.payment.PaymentStatus;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private static final String FROM_EMAIL = "stayhub.booking.online@gmail.com";
    private static final String FROM_NAME = "Stayhub.com";
    private static final String SUPPORT_PHONE = "1900 8080";
    private static final String SUPPORT_EMAIL = "stayhub.booking.online@gmail.com";

    private final JavaMailSender mailSender;

    public void send(String to, String email, String subject) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            helper.setText(email, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(FROM_EMAIL, FROM_NAME);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("failed to send email");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String buildEmailBookingSuccess(MailBookingSuccess m) {
        String body = """
            %s
            %s
            %s
            %s
            %s
            """.formatted(
                greeting(m.getCustomerName(),
                        "Đơn đặt phòng của quý khách đã được xác nhận. Dưới đây là thông tin chi tiết:"),
                infoTable(
                        row("Mã đặt phòng", m.getBookingId()),
                        row("Tên khách sạn", m.getHotelName()),
                        row("Ngày nhận phòng", String.valueOf(m.getCheckInDate())),
                        rowLast("Ngày trả phòng", String.valueOf(m.getCheckOutDate()))
                ),
                roomsSection("Các phòng đã đặt", m.getRooms())
                        + totalTable(m.getTotalPrice())
                        + paymentStatusBadge(m.getPaymentStatus()),
                qrCheckInBlock(m.getBookingCode()),
                note("Vui lòng mang theo giấy tờ tùy thân hợp lệ khi nhận phòng. Nếu cần thay đổi hoặc " +
                        "hủy đặt phòng, quý khách vui lòng liên hệ với chúng tôi trước ít nhất 24 giờ.")
        );

        return wrapEmail(
                "Xác nhận đặt phòng thành công",
                "#0f766e", "Đặt phòng thành công!", "Cảm ơn quý khách đã tin tưởng lựa chọn chúng tôi",
                "#d1fae5", "#0f766e", "✓",
                body
        );
    }

    public String buildEmailBookingCanceled(MailBookingCanceled m) {
        String refundBadge = m.getPaymentStatus().equals(PaymentStatus.PAID)
                ? statusBox("Trạng thái hoàn tiền", "Đang xử lý hoàn tiền",
                "Email xác nhận hoàn tiền chi tiết sẽ được gửi riêng ngay khi xử lý xong.")
                : "";

        String body = """
            %s
            %s
            %s
            %s
            %s
            """.formatted(
                greeting(m.getCustomerName(),
                        "Đơn đặt phòng của quý khách đã được hủy thành công theo yêu cầu. Chi tiết như sau:"),
                infoTable(
                        row("Mã đặt phòng", strike(m.getBookingId())),
                        row("Tên khách sạn", m.getHotelName()),
                        row("Ngày nhận phòng (đã hủy)", String.valueOf(m.getCheckInDate())),
                        row("Ngày trả phòng (đã hủy)", String.valueOf(m.getCheckOutDate())),
                        rowLast("Thời gian hủy", String.valueOf(LocalDate.now()))
                ),
                roomsSection("Các phòng đã hủy", m.getRooms()),
                refundBadge,
                note("Nếu quý khách không thực hiện yêu cầu hủy này hoặc cần hỗ trợ thêm, vui lòng liên hệ " +
                        "với chúng tôi ngay để được kiểm tra và hỗ trợ kịp thời.")
        );

        return wrapEmail(
                "Xác nhận hủy đặt phòng",
                "#dc2626", "Đặt phòng đã được hủy", "Chúng tôi rất tiếc vì đã không thể đón tiếp quý khách lần này",
                "#fee2e2", "#dc2626", "✕",
                body
        );
    }

    public String buildEmailPaymentSuccess(MailPaymentSuccess m) {
        String body = """
            %s
            %s
            %s
            """.formatted(
                greeting(m.getCustomerName(),
                        "Chúng tôi đã nhận được thanh toán cho đơn đặt phòng <strong>" + m.getBookingId() +
                                "</strong>. Chi tiết giao dịch như sau:"),
                infoTable(
                        row("Mã giao dịch", m.getTransactionCode()),
                        row("Mã đặt phòng liên kết", m.getBookingId()),
                        row("Phương thức thanh toán", m.getPaymentMethod().name()),
                        row("Thời gian thanh toán", String.valueOf(m.getPaidAt())),
                        rowLastHighlight("Số tiền đã thanh toán", String.valueOf(m.getAmount()), "#166534")
                ),
                note("Đây là email xác nhận giao dịch thanh toán. Vui lòng lưu lại làm bằng chứng thanh toán " +
                        "khi cần đối soát hoặc khiếu nại.")
        );

        return wrapEmail(
                "Thanh toán thành công",
                "#166534", "Thanh toán thành công!", "Cảm ơn quý khách, giao dịch của bạn đã được xử lý",
                "#d1fae5", "#166534", "✓",
                body
        );
    }

    public String buildEmailPaymentRefunding(MailPaymentRefunding m) {
        String body = """
            %s
            %s
            %s
            %s
            %s
            """.formatted(
                greeting("{{TEN_KHACH_HANG}}",
                        "Yêu cầu hoàn tiền cho đơn đặt phòng <strong>{{MA_DAT_PHONG}}</strong> (đã hủy) đã được " +
                                "xử lý. Chi tiết như sau:"),
                infoTable(
                        row("Mã hoàn tiền", "{{MA_HOAN_TIEN}}"),
                        row("Mã đặt phòng liên kết", "{{MA_DAT_PHONG}}"),
                        row("Số tiền đã thanh toán", "{{SO_TIEN_DA_THANH_TOAN}}"),
                        row("Phí hủy (nếu có)", "<span style=\"color:#dc2626;\">-{{PHI_HUY}}</span>"),
                        row("Phương thức hoàn tiền", "{{PHUONG_THUC_THANH_TOAN}}"),
                        rowLastHighlight("Số tiền được hoàn", "{{SO_TIEN_HOAN_LAI}}", "#1e40af")
                ),
                noteBox("Số tiền hoàn sẽ được chuyển về phương thức thanh toán ban đầu trong vòng " +
                        "{{SO_NGAY_HOAN_TIEN}} ngày làm việc. Thời gian thực tế có thể thay đổi tùy theo " +
                        "ngân hàng hoặc đơn vị trung gian thanh toán.")
                        + ctaButton("{{DUONG_DAN_LIEN_HE}}", "Liên hệ hỗ trợ"),
                note("Nếu sau {{SO_NGAY_HOAN_TIEN}} ngày làm việc quý khách vẫn chưa nhận được khoản hoàn tiền, " +
                        "vui lòng liên hệ với chúng tôi để được hỗ trợ kiểm tra.")
        );

        return wrapEmail(
                "Xác nhận đang hoàn tiền",
                "#1e40af", "Xác nhận hoàn tiền", "Khoản hoàn tiền của quý khách đã được xử lý",
                "#dbeafe", "#1e40af", "\uD83D\uDCB3",
                body
        );
    }

    private String wrapEmail(String title, String headerBg, String headerTitle, String headerSubtitle,
                             String iconBg, String iconColor, String icon, String bodyHtml) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>%s</title>
            </head>
            <body style="margin:0; padding:0; background-color:#f2f4f7; font-family: Arial, Helvetica, sans-serif;">
              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f2f4f7; padding:30px 0;">
                <tr>
                  <td align="center">
                    <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:8px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.06);">
                      <tr>
                        <td style="background-color:%s; padding:28px 32px; text-align:center;">
                          <h1 style="margin:0; color:#ffffff; font-size:22px; font-weight:600;">%s</h1>
                          <p style="margin:6px 0 0; color:#d1fae5; font-size:14px;">%s</p>
                        </td>
                      </tr>
                      <tr>
                        <td style="text-align:center; padding:24px 32px 0;">
                          <div style="width:56px; height:56px; border-radius:50%%; background-color:%s; display:inline-block; line-height:56px; font-size:28px; color:%s;">%s</div>
                        </td>
                      </tr>
                      %s
                      <tr><td style="border-top:1px solid #e5e7eb;"></td></tr>
                      %s
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>""".formatted(title, headerBg, headerTitle, headerSubtitle, iconBg, iconColor, icon,
                bodyHtml, footer());
    }

    private String footer() {
        return """
            <tr>
              <td style="padding:24px 32px; text-align:center;">
                <p style="font-size:13px; color:#9ca3af; margin:0 0 4px;">Cần hỗ trợ? Liên hệ chúng tôi qua</p>
                <p style="font-size:13px; color:#374151; margin:0;">
                  \uD83D\uDCDE %s &nbsp;|&nbsp; ✉\uFE0F %s
                </p>
                <p style="font-size:12px; color:#9ca3af; margin:16px 0 0;">© 2026 Stay Hub. All rights reserved.</p>
              </td>
            </tr>""".formatted(SUPPORT_PHONE, SUPPORT_EMAIL);
    }

    private String greeting(String customerName, String message) {
        return """
            <tr>
              <td style="padding:16px 32px 0;">
                <p style="font-size:15px; color:#111827; line-height:1.6;">
                  Xin chào <strong>%s</strong>,<br>
                  %s
                </p>
              </td>
            </tr>""".formatted(customerName, message);
    }

    private String row(String label, String value) {
        return """
            <tr>
              <td style="padding:16px 20px; border-bottom:1px solid #f1f5f9; font-size:14px; color:#6b7280; width:45%%;">%s</td>
              <td style="padding:16px 20px; border-bottom:1px solid #f1f5f9; font-size:14px; color:#111827; font-weight:600; text-align:right;">%s</td>
            </tr>""".formatted(label, value);
    }

    private String rowLast(String label, String value) {
        return """
            <tr>
              <td style="padding:16px 20px; font-size:14px; color:#6b7280;">%s</td>
              <td style="padding:16px 20px; font-size:14px; color:#111827; font-weight:600; text-align:right;">%s</td>
            </tr>""".formatted(label, value);
    }

    private String rowLastHighlight(String label, String value, String color) {
        return """
            <tr>
              <td style="padding:16px 20px; font-size:14px; color:#6b7280;">%s</td>
              <td style="padding:16px 20px; font-size:16px; color:%s; font-weight:700; text-align:right;">%s</td>
            </tr>""".formatted(label, color, value);
    }

    private String infoTable(String... rows) {
        return """
            <tr>
              <td style="padding:16px 32px 8px;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="border:1px solid #e5e7eb; border-radius:8px;">
                  %s
                </table>
              </td>
            </tr>""".formatted(String.join("\n", rows));
    }

    private String strike(String value) {
        return "<span style=\"text-decoration:line-through; opacity:0.7;\">" + value + "</span>";
    }

    private String note(String text) {
        return """
            <tr>
              <td style="padding:0 32px 24px;">
                <p style="font-size:13px; color:#6b7280; line-height:1.6; margin:0;">%s</p>
              </td>
            </tr>""".formatted(text);
    }

    private String noteBox(String text) {
        return """
            <tr>
              <td style="padding:0 32px 16px;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#eff6ff; border:1px solid #bfdbfe; border-radius:8px;">
                  <tr>
                    <td style="padding:14px 20px;">
                      <p style="margin:0; font-size:12px; color:#1e40af; line-height:1.5;">%s</p>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>""".formatted(text);
    }

    private String ctaButton(String href, String label) {
        return """
            <tr>
              <td style="padding:8px 32px 24px; text-align:center;">
                <a href="%s" style="display:inline-block; background-color:#0f766e; color:#ffffff; text-decoration:none; padding:12px 24px; border-radius:6px; font-size:14px; font-weight:600;">%s</a>
              </td>
            </tr>""".formatted(href, label);
    }

    private String totalTable(BigDecimal totalPrice) {
        return """
            <tr>
              <td style="padding:8px 32px 16px;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="border:1px solid #e5e7eb; border-radius:8px;">
                  <tr>
                    <td style="padding:16px 20px; font-size:14px; color:#6b7280;">Tổng thanh toán</td>
                    <td style="padding:16px 20px; font-size:16px; color:#0f766e; font-weight:700; text-align:right;">%s</td>
                  </tr>
                </table>
              </td>
            </tr>""".formatted(totalPrice);
    }

    private String qrCheckInBlock(String bookingCode) {
        return """
            <tr>
              <td style="padding:0 32px 16px;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="border:1px dashed #0f766e; border-radius:8px;">
                  <tr>
                    <td style="padding:20px; text-align:center;">
                      <p style="margin:0 0 4px; font-size:14px; color:#111827; font-weight:700;">Mã QR check-in</p>
                      <p style="margin:0 0 14px; font-size:12px; color:#6b7280;">Xuất trình mã này tại quầy lễ tân để nhận phòng nhanh chóng</p>
                      <img src="https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=%s" alt="Mã QR check-in %s" width="180" height="180" style="display:block; margin:0 auto; border:1px solid #e5e7eb; border-radius:8px;">
                      <p style="margin:14px 0 0; font-size:13px; color:#111827; font-weight:600;">%s</p>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>""".formatted(bookingCode, bookingCode, bookingCode);
    }

    private String statusBox(String label, String value, String subText) {
        String sub = subText == null ? "" :
                "<p style=\"margin:4px 0 0; font-size:12px; color:#9ca3af;\">" + subText + "</p>";
        return """
            <tr>
              <td style="padding:0 32px 16px;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f9fafb; border:1px solid #e5e7eb; border-radius:8px;">
                  <tr>
                    <td style="padding:14px 20px; text-align:center;">
                      <p style="margin:0; font-size:13px; color:#374151;">%s: <strong style="color:#111827;">%s</strong></p>
                      %s
                    </td>
                  </tr>
                </table>
              </td>
            </tr>""".formatted(label, value, sub);
    }

    private String paymentStatusBadge(PaymentStatus status) {
        return status.equals(PaymentStatus.PAID)
                ? statusBox("Trạng thái thanh toán", "Đã thanh toán",
                "Email xác nhận thanh toán riêng sẽ được gửi ngay khi giao dịch hoàn tất.")
                : statusBox("Trạng thái thanh toán", "Chờ thanh toán (tiền mặt)", null);
    }

    private String roomsSection(String title, List<InternalFullRoomTypeDto> rooms) {
        return """
            <tr>
              <td style="padding:0 32px 8px;">
                <p style="margin:0 0 8px; font-size:13px; color:#6b7280; font-weight:700; text-transform:uppercase; letter-spacing:0.03em;">%s</p>
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="border:1px solid #e5e7eb; border-radius:8px; overflow:hidden;">
                  <tr style="background-color:#f9fafb;">
                    <td style="padding:10px 16px; font-size:12px; color:#6b7280; font-weight:700;"></td>
                    <td style="padding:10px 16px; font-size:12px; color:#6b7280; font-weight:700;">Loại phòng</td>
                    <td style="padding:10px 12px; font-size:12px; color:#6b7280; font-weight:700; text-align:center;">SL</td>
                    <td style="padding:10px 12px; font-size:12px; color:#6b7280; font-weight:700; text-align:center;">Số đêm</td>
                    <td style="padding:10px 16px; font-size:12px; color:#6b7280; font-weight:700; text-align:right;">Thành tiền</td>
                  </tr>
                  %s
                </table>
              </td>
            </tr>""".formatted(title, buildRoomRows(rooms));
    }

    private String buildRoomRows(List<InternalFullRoomTypeDto> rooms) {
        if (rooms == null || rooms.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (InternalFullRoomTypeDto r : rooms) {
            Integer quantity = r.getInventories().getFirst().getAvailableQuantity();
            int nights = r.getInventories().size();
            BigDecimal totalUnit = BigDecimal.ZERO;
            for (InternalRoomInventoryDto inv : r.getInventories()) {
                totalUnit = totalUnit.add(inv.getPrice());
            }
            BigDecimal lineTotal = totalUnit.multiply(BigDecimal.valueOf(quantity));

            sb.append("""
                <tr>
                  <td style="padding:12px 16px; border-top:1px solid #f1f5f9; text-align:center;"><img src="%s" alt="%s" width="60" height="60" style="width:60px; height:60px; object-fit:cover; border-radius:6px; display:block;"/></td>
                  <td style="padding:12px 16px; border-top:1px solid #f1f5f9; font-size:13px; color:#111827; font-weight:600;">%s</td>
                  <td style="padding:12px 12px; border-top:1px solid #f1f5f9; font-size:13px; color:#111827; text-align:center;">%s</td>
                  <td style="padding:12px 12px; border-top:1px solid #f1f5f9; font-size:13px; color:#111827; text-align:center;">%s</td>
                  <td style="padding:12px 16px; border-top:1px solid #f1f5f9; font-size:13px; color:#111827; text-align:right; font-weight:600;">%s</td>
                </tr>
                """.formatted(r.getUrlImage(), r.getName(), r.getName(), quantity, nights, lineTotal));
        }
        return sb.toString();
    }
}