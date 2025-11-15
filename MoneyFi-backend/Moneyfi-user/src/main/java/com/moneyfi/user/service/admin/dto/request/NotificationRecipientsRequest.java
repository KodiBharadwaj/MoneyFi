package com.moneyfi.user.service.admin.dto.request;

import com.moneyfi.user.model.dto.UserAuthModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecipientsRequest {
    private Long id;
    private List<UserAuthModel> recipients;
}
