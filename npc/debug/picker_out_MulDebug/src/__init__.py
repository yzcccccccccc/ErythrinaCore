from mlvp.reporter import *

# Start of the test case here！
if __name__ == '__main__':
    set_meta_info('test_case', 'multiplier')
    report = "report/report.html"
    generate_pytest_report(report, ['-s'])

    print('Hello, MLVP! :D')