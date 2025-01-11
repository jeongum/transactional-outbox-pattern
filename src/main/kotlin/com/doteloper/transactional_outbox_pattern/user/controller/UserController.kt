package com.doteloper.transactional_outbox_pattern.user.controller

import com.doteloper.transactional_outbox_pattern.user.service.UserCreateService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController(
    private val userCreateService: UserCreateService,
) {
    @PostMapping
    fun join() {
        userCreateService.create("Some Name")
    }
}
