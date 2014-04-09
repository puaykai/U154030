# -*- coding: utf-8 -*-
"""
Created on Wed Jan 29 18:34:58 2014

@author: puaykai
"""

from sampleDATA import sampleDATA
from number_to_binary_string import to_binary
from number_to_binary_string import from_binary
import modified_LZW as lzw
import time
from LZW_experiment import LZW_experiment as lzw_experiment
#from LTC_experiment import LTC_experiment as ltc_experiment
#from actual_LTC_experiment import LTC_experiment as actual_ltc_experiment
import numpy as np

#[training, testing] = sampleDATA()

#print "Got data", shape(testing)

################LZW
"""
This will output:
(1) bits used in the file name
(2) compression ratio in the first co

(1) data_index in the file name, i.e. the row in the data
(2) Errors in the first row
(3) Each other row will be DICTIONARY_SIZE, followed by compression ratio
"""

minimum_bit_size = 15
LZW_results_file_path = "C:\\Users\puaykai\\Desktop\\UROP\\LZW results\\"

for number_of_bits in range(1):
    new_array = []
    truncated_array = []
    #for data_index in range(shape(testing)[0]):
    
    data = [ 87.3, 87.12, 87.01, 87.14, 86.79, 86.59, 86.36, 86.8, 86.85, 86.66, 87.06, 87.03, 87.18, 87.23, 87.45, 87.59, 87.77, 87.77, 88.08, 88.52, 88.27, 88.36, 88.66, 88.8, 88.73, 88.76, 88.56, 88.63, 88.62, 88.72, 88.61, 88.63, 88.78, 88.99, 88.83, 89.03, 88.69, 88.86, 88.62, 88.56, 88.3, 88.21, 88.6, 88.21, 88.43, 88.04, 88.37, 88.56, 88.26, 88.52, 88.33, 88.57, 88.9, 89.15, 89.22, 89.8, 90.36, 90.37, 90.71, 90.65, 91.24, 91.24, 91.46, 91.4, 91.73, 91.32, 91.77, 91.68, 91.65, 91.64, 91.81, 91.79, 91.64, 91.68, 91.63, 91.41, 91.32, 91.48, 91.58, 91.67, 91.81, 92.09, 91.47, 91.1, 91.1, 91.34, 91.38, 91.59, 91.32, 91.48, 91.3, 91.42, 91.6, 91.78, 91.69, 91.89, 91.92, 92.04, 92.23, 91.96, 92.08, 92.33, 92.37, 91.71, 92.03, 92.19, 92.17, 92.25, 92.59, 92.71, 92.7, 92.83, 93., 93.04, 93.17, 93.31, 93.62, 93.85, 93.83, 93.61, 93.54, 93.62, 93.91, 94.07, 94.09, 94.04, 95.89, 95.9, 94., 93.9, 93.98, 94.07, 94.07, 94.28, 94.29, 94.37, 94.53, 94.33, 94.33, 94.51, 94.73, 94.77, 94.81, 94.84, 95.14, 95.31, 95.35, 95.27, 94.84, 94.74, 94.66, 94.63, 94.42, 94.38, 94.36, 94.29, 94.02, 94.1, 94.07, 94.09, 94.1, 94.26, 94.35, 94.34, 94.14, 94.11, 94., 93.97, 93.98, 93.99, 94., 94.08, 94.22, 94.3, 94.57, 95.05, 95.24, 95.32, 95.27, 95.15, 95.21, 95.23, 95.15, 95.26, 95.4, 95.47, 95.43, 95.13, 94.65, 94.41, 93.89, 93.39, 93.27, 93.43, 93.51, 93.37, 93.43, 93.55, 93.69, 93.72]#testing[:][data_index]
    
        #print "data:", data.shape
        
    converted_data = map(lambda x: to_binary(x,number_of_bits+minimum_bit_size+1), data)
    #print converted_data
    new_array = new_array +[converted_data] #converts reading to binary
    
    for bit_string in converted_data:
        truncated_array = truncated_array + [from_binary(bit_string)]
    #truncated_array = truncated_array + [map(from_binary, converted_data)]
        #converts back and calculates the errors
    print truncated_array
    errors=[]#    errors = testing - truncated_array
    #writes error to file
    #print "writing error for: ", number_of_bits
    #f_handle = file(LZW_results_file_path+str(number_of_bits+minimum_bit_size+1)+".csv",'a')
    #for row in errors:
    #    f_handle.write(''.join([str(error)+"," for error in row])+"\n")
    #f_handle.close()
    
    #print "shape(testing): ", shape(testing)
   # print "shape(truncated_array): ",shape(truncated_array)
   # print "shape(errors): ",shape(errors)
    #print truncated_array
    print new_array
    returned_data = lzw_experiment(errors,new_array,LZW_results_file_path+str(number_of_bits+minimum_bit_size+1)+"_")
    
    print map(from_binary, [returned_data[i:i+minimum_bit_size+1] for i in range(0, len(returned_data)/(minimum_bit_size+1))] )
#print shape(new_array)
###############LZW

####################LTC compress by station
"""
This will output:
(1) Epsilon in the file name
(2) Compression ratio on the first column of each row
(3) Errors after decompressing for each column of each row, according to the 48 stations

So it should have 
"""
"""
LTC_results_file_path = 'C:\\Users\\puaykai\\Desktop\\UROP\\LTC results\\'

for epsilon in frange(0.070,0.07,0.001):
    print "experimenting: ", epsilon
    
    ltc_experiment(testing, epsilon, LTC_results_file_path+str(epsilon)+".csv")

####################LTC compress by station
"""
#acutal LTC
"""
LTC_results_file_path = 'C:\\Users\\puaykai\\Desktop\\UROP\\Actual LTC results\\'

for epsilon in frange(0.001,1,0.001):
    print "experimenting: ", epsilon
    
    actual_ltc_experiment(testing, epsilon, LTC_results_file_path+str(epsilon)+".csv")
"""
####################SAE

####################SAE