// SPDX-FileCopyrightText: Copyright 2026 Eden Emulator Project
// SPDX-License-Identifier: GPL-3.0-or-later

// SPDX-FileCopyrightText: Copyright 2024 yuzu Emulator Project
// SPDX-License-Identifier: GPL-3.0-or-later

#pragma once

#include <algorithm>
#include <array>
#include <cctype>
#include <string_view>

#include "common/logging.h"

#include "core/hle/service/bcat/bcat_result.h"
#include "core/hle/service/bcat/bcat_types.h"

namespace Service::BCAT {

// For a name to be valid it must be non-empty, must have a null terminating character as the final
// char, can only contain numbers, letters, underscores and a hyphen if directory and a period if
// file.
// Only the characters before the terminator are checked: bytes after it are unused and may hold
// leftovers from the caller's buffer.
constexpr Result VerifyNameValidInternal(std::array<char, 0x20> name, char match_char) {
    const auto end = std::find(name.begin(), name.end(), '\0');
    const bool empty_or_unterminated = end == name.begin() || end == name.end();
    const bool bad_chars = std::any_of(name.begin(), end, [match_char](char c) {
        return !std::isalnum(static_cast<u8>(c)) && c != '_' && c != match_char;
    });
    if (empty_or_unterminated || bad_chars) {
        LOG_ERROR(Service_BCAT, "Name passed was invalid: {}",
                  std::string_view(name.data(), static_cast<size_t>(end - name.begin())));
        return ResultInvalidArgument;
    }

    return ResultSuccess;
}

constexpr Result VerifyNameValidDir(DirectoryName name) {
    return VerifyNameValidInternal(name, '-');
}

constexpr Result VerifyNameValidFile(FileName name) {
    return VerifyNameValidInternal(name, '.');
}

} // namespace Service::BCAT
