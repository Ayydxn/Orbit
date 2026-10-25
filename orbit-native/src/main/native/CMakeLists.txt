cmake_minimum_required(VERSION 4.2)
project(OrbitJNIBridge)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ── Compiler hardening & optimisation flags ──────────────────────────────────
if(MSVC)
    add_compile_options(/O2 /GL /arch:AVX2 /W4)
    add_link_options(/LTCG)
else()
    add_compile_options(-O3 -march=native -flto -fvisibility=hidden -Wall)
    add_link_options(-flto)
endif()

# ── Locate JNI ───────────────────────────────────────────────────────────────
find_package(JNI REQUIRED)

# ── Paths passed in from Gradle ──────────────────────────────────────────────
# ULTRALIGHT_SDK  — root of the user's Ultralight SDK

set(UL_INCLUDE "${ULTRALIGHT_SDK}/include")
set(UL_LIB_DIR "${ULTRALIGHT_SDK}/lib")

# ── Sources ──────────────────────────────────────────────────────────────────
file(GLOB_RECURSE SOURCES "src/*.cpp" "src/*.h")

add_library(OrbitJNIBridge SHARED ${SOURCES})

target_include_directories(OrbitJNIBridge PRIVATE
        ${CMAKE_SOURCE_DIR}
        ${JNI_INCLUDE_DIRS}
        ${UL_INCLUDE}
)

# Link against Ultralight (user-provided)
target_link_directories(OrbitJNIBridge PRIVATE ${UL_LIB_DIR})
target_link_libraries(OrbitJNIBridge PRIVATE
        Ultralight
        UltralightCore
        WebCore
)

# Export only JNI symbols, keep everything else hidden
set_target_properties(OrbitJNIBridge PROPERTIES
        CXX_VISIBILITY_PRESET hidden
        VISIBILITY_INLINES_HIDDEN ON
)
