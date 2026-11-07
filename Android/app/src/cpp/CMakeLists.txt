cmake_minimum_required(VERSION 3.22.1)
project(re3_android LANGUAGES C CXX)

add_library(re3_android SHARED native-lib.cpp)

find_library(log-lib log)
target_link_libraries(re3_android ${log-lib})
target_compile_features(re3_android PRIVATE cxx_std_17)

# TODO: adicionar aqui os targets e fontes do runtime PSXRecomp Android.
