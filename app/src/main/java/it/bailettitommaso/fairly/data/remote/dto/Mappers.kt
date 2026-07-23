package it.bailettitommaso.fairly.data.remote.dto

import it.bailettitommaso.fairly.domain.model.Role
import it.bailettitommaso.fairly.domain.model.User

fun MeDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    role = Role.fromApi(role),
    mustChangePassword = mustChangePassword,
)

fun UserDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    role = Role.fromApi(role),
    mustChangePassword = false,
)
