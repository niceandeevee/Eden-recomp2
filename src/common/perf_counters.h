// SPDX-FileCopyrightText: Copyright 2026 Eden Emulator Project
// SPDX-License-Identifier: GPL-3.0-or-later

#pragma once

#include <atomic>

#include "common/common_types.h"

namespace Common {

/// Guest writes that faulted on GPU-cached memory under NCE (each one is a signal).
inline std::atomic<u64> g_guest_write_faults{0};
/// Times a memory region was (un)protected because the GPU cached or released it.
inline std::atomic<u64> g_gpu_cache_protects{0};

} // namespace Common
