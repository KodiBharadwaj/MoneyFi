package com.moneyfi.user.repository.admin.impl;

import com.moneyfi.user.exceptions.QueryValidationException;
import com.moneyfi.user.repository.admin.AdminRepository;
import com.moneyfi.user.service.admin.dto.response.*;
import com.moneyfi.user.service.common.dto.response.UserFeedbackResponseDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AdminRepositoryImpl implements AdminRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public AdminOverviewPageDto getAdminOverviewPageDetails() {
        try {
            Query query = entityManager.createNativeQuery(
                    "exec getAdminOverviewPageDetails ")
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(AdminOverviewPageDto.class));

            return (AdminOverviewPageDto) query.getSingleResult();
        } catch (Exception e){
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching admin overview page details");
        }
    }

    @Override
    public List<UserRequestsGridDto> getUserRequestsGridForAdmin(String requestReason) {
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getUserRequestsGridForAdmin " +
                                    "@requestReason = :requestReason")
                    .setParameter("requestReason", requestReason)
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(UserRequestsGridDto.class));
            return query.getResultList();
        } catch (Exception e){
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching user request details raised by users'");
        }
    }

    @Override
    public List<UserGridDto> getUserDetailsGridForAdmin(String status) {
        List<UserGridDto> userGridDetails = new ArrayList<>();
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getUserGridDetailsByStatusForAdmin " +
                            "@status = :status")
                    .setParameter("status", status)
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(UserGridDto.class));
            userGridDetails.addAll(query.getResultList());
        } catch (Exception e){
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching fetching " + status.toLowerCase() + " user grid details");
        }
        return userGridDetails;
    }

    @Override
    public Map<Integer, Integer> getUserMonthlyCountInAYear(int year, String status) {
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getUserMonthlyCountInAYear " +
                                    "@year = :year")
                    .setParameter("year", year)
                    .unwrap(NativeQuery.class);

            List<Object[]> resultList = query.getResultList();

            Map<Integer, Integer> resultMap = new HashMap<>();
            for (Object[] row : resultList) {
                Integer month = (Integer) row[0];
                Integer count = (Integer) row[1];
                resultMap.put(month, count);
            }
            return resultMap;
        } catch (Exception e){
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching user monthly count in a year");
        }
    }

    @Override
    public UserProfileAndRequestDetailsDto getCompleteUserDetailsForAdmin(String username) {
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getCompleteUserDetailsForAdmin " +
                                    "@username = :username")
                    .setParameter("username", username)
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(UserProfileAndRequestDetailsDto.class));

            return (UserProfileAndRequestDetailsDto) query.getSingleResult();
        } catch (Exception e){
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching user complete details for admin");
        }
    }

    @Override
    public List<UserDefectResponseDto> getUserRaisedDefectsForAdmin() {
        List<UserDefectResponseDto> userDefectResponseDtosList = new ArrayList<>();
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getUserRaisedDefectsForAdmin ")
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(UserDefectResponseDto.class));

            userDefectResponseDtosList.addAll(query.getResultList());
            return userDefectResponseDtosList;
        } catch (Exception e){
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching fetching user raised defect details");
        }
    }

    @Override
    public List<UserFeedbackResponseDto> getUserFeedbackListForAdmin() {
        List<UserFeedbackResponseDto> userFeedbackList = new ArrayList<>();
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getUserFeedbackListForAdmin ")
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(UserFeedbackResponseDto.class));

            userFeedbackList.addAll(query.getResultList());
            return userFeedbackList;
        } catch (Exception e){
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching user feedback details");
        }
    }

    @Override
    public List<AdminSchedulesResponseDto> getAllActiveSchedulesOfAdmin(String status) {
        List<AdminSchedulesResponseDto> scheduleList = new ArrayList<>();
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getAllActiveSchedulesOfAdmin " +
                                    "@status = :status")
                    .setParameter("status", status)
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(AdminSchedulesResponseDto.class));
            scheduleList.addAll(query.getResultList());
            return scheduleList;
        } catch (Exception e){
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching schedules for admin");
        }
    }
}
