package com.nemchann.fitnessbackend.schedule.service;

import com.nemchann.fitnessbackend.schedule.repository.RoomRepository;
import com.nemchann.fitnessbackend.schedule.repository.ScheduleRepository;
import com.nemchann.fitnessbackend.schedule.repository.WorkoutRepository;
import com.nemchann.fitnessbackend.schedule.repository.WorkoutTypeRepository;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {
    private RoomRepository roomRepository;
    private ScheduleRepository scheduleRepository;
    private WorkoutRepository workoutRepository;
    private WorkoutTypeRepository workoutTypeRepository;
}
