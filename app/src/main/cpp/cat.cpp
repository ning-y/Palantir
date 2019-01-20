#include <string>
#include <fstream>
#include <iostream>

std::string get_file_contents(std::string);

int main(int argc, char* argv[]) {
    // There is always at least one argument, the path to this binary itself
    // So, the path to file we are interested to print is the second arg, argv[1].
    if (argc <= 1) {
        std::cerr << "Please pass the path to a text file as argument" << "\n";
        exit(1);
    }

    std::string contents = get_file_contents(argv[1]);
    std::cout << contents;
    exit(0);
}

std::string get_file_contents(std::string abs_path) {
    std::ifstream inFile;
    inFile.open(abs_path);

    if (!inFile) {
        std::cerr << "An error occurred." << "\n";
        exit(1);
    }

    std::string contents;
    std::string buffer;
    while (std::getline(inFile, buffer)) {
        contents = contents + buffer + "\n";
    }

    inFile.close();
    return contents;
}
