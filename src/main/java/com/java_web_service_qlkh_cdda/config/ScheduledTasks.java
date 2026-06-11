package com.java_web_service_qlkh_cdda.config;

import com.java_web_service_qlkh_cdda.entity.Submission;
import com.java_web_service_qlkh_cdda.enums.StatusEnum;
import com.java_web_service_qlkh_cdda.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {
    private final SubmissionRepository submissionRepository;

    /**


    /**
     * Every 5 minutes: auto-transition PENDING submissions to LATE if deadline passed
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void autoMarkLateSubmissions() {
        List<Submission> pendingSubmissions = submissionRepository.findByStatus(StatusEnum.PENDING);
        long count = pendingSubmissions.stream()
                .filter(s -> LocalDateTime.now().isAfter(s.getAssignment().getDeadline()))
                .peek(s -> s.setStatus(StatusEnum.LATE))
                .peek(submissionRepository::save)
                .count();
        if (count > 0) {
            log.info("[SCHEDULER] Auto-marked {} submission(s) as LATE", count);
        }
    }
}