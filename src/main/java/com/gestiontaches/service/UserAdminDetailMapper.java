package com.gestiontaches.service;

import com.gestiontaches.domain.Authority;
import com.gestiontaches.domain.ProjectMember;
import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.TaskHistory;
import com.gestiontaches.domain.User;
import com.gestiontaches.service.dto.UserAdminDetailDTO;
import com.gestiontaches.service.dto.UserAdminDetailDTO.ProjectMembershipDTO;
import com.gestiontaches.service.dto.UserAdminDetailDTO.TaskHistoryEntryDTO;
import com.gestiontaches.service.dto.UserAdminDetailDTO.TaskSummaryDTO;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Mapper for the {@link UserAdminDetailDTO}.
 * Hand-coded to handle the complex assembly of the admin detail view.
 */
@Service
public class UserAdminDetailMapper {

    public UserAdminDetailDTO toDto(User user, List<Task> tasks, List<ProjectMember> memberships, List<TaskHistory> history) {
        UserAdminDetailDTO dto = new UserAdminDetailDTO();
        dto.setId(user.getId());
        dto.setLogin(user.getLogin());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setActivated(user.isActivated());
        dto.setLangKey(user.getLangKey());
        dto.setCreatedBy(user.getCreatedBy());
        dto.setCreatedDate(user.getCreatedDate());
        dto.setAuthorities(user.getAuthorities().stream().map(Authority::getName).collect(Collectors.toSet()));

        dto.setTasks(tasks.stream().map(this::toTaskSummary).toList());
        dto.setProjects(memberships.stream().map(this::toProjectMembership).toList());
        dto.setRecentActivity(history.stream().map(this::toHistoryEntry).toList());

        return dto;
    }

    private TaskSummaryDTO toTaskSummary(Task task) {
        TaskSummaryDTO dto = new TaskSummaryDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setCreatedAt(task.getCreatedAt());

        if (task.getProject() != null) {
            dto.setProjectId(task.getProject().getId());
            dto.setProjectName(task.getProject().getName());
            dto.setProjectKey(task.getProject().getKey());
        }
        if (task.getSprint() != null) {
            dto.setSprintId(task.getSprint().getId());
            dto.setSprintName(task.getSprint().getName());
        }
        if (task.getEpic() != null) {
            dto.setEpicId(task.getEpic().getId());
            dto.setEpicTitle(task.getEpic().getTitle());
        }
        return dto;
    }

    private ProjectMembershipDTO toProjectMembership(ProjectMember pm) {
        ProjectMembershipDTO dto = new ProjectMembershipDTO();
        dto.setProjectId(pm.getProject().getId());
        dto.setProjectName(pm.getProject().getName());
        dto.setProjectKey(pm.getProject().getKey());
        dto.setRole(pm.getRole());
        dto.setJoinedAt(pm.getJoinedAt());
        return dto;
    }

    private TaskHistoryEntryDTO toHistoryEntry(TaskHistory th) {
        TaskHistoryEntryDTO dto = new TaskHistoryEntryDTO();
        dto.setId(th.getId());
        dto.setAction(th.getAction());
        dto.setOldValue(th.getOldValue());
        dto.setNewValue(th.getNewValue());
        dto.setCreatedAt(th.getCreatedAt());
        if (th.getTask() != null) {
            dto.setTaskId(th.getTask().getId());
            dto.setTaskTitle(th.getTask().getTitle());
        }
        return dto;
    }
}
