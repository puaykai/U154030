# -*- coding: utf-8 -*-
"""
Created on Tue Jan 28 18:59:10 2014

@author: puaykai
"""

import another_LZW as LZW
import numpy as np

#testing - ( number_of_data, data_dimension), assumed to be tuncated and each entry is in binary string
def LZW_experiment(errors,testing, file_path):
    
    last_compression_ratio = 1000
    dictionary_range = 1
    list_of_dictionary_size = []#list of dictionary according to dictionary_size
    ORIGINAL_DICTIONARY_SIZE = 258+102400
    #print testing
    #writes error only once, then varies the 
    #file_handle = file(file_path+".csv", 'a')
    for dictionary_size in range(dictionary_range):
        print "testing dictionary size: ", dictionary_size
        #file_handle = file(file_path+str(dictionary_size+ORIGINAL_DICTIONARY_SIZE)+".csv", 'a')
        
        #TODO:start timing
        
        #compression_ratios = []
        ratio_sum = 0
        
        number_of_datas = np.shape(testing)[0]
        
        for data_index in range(number_of_datas):
            
            if data_index%10000 == 0:
                print "AT DATA_INDEX:", data_index
            
            data = testing[:][data_index]
            length_of_data_point = len(data[0])
            ####################################################
            #Combining the 48 station's readings into one long binary string
            data = ''.join(data)
            print "data: ",data
            #print "___________________________________________________________"
            
            ###############################################################
            #Compression process
            compressed_message = ""
            temp = LZW.compress(data, dictionary_size+ORIGINAL_DICTIONARY_SIZE)
            print temp
            for i in temp:
                compressed_message = compressed_message+str(i)
            
            length_of_compressed_message = len(compressed_message)
            compression_ratio = (length_of_compressed_message+0.0) / len(data)
            
            
            
            temp = LZW.decompress(temp)            
            print temp == data
            
            
            ################################################################
            #compression_ratios = compression_ratios + [compression_ratio]
            ratio_sum = ratio_sum + compression_ratio
            ##################################################################
            #uncompressing, checking
            
            #uncompressed_message = ""
            #for i in LZW.decompress(compressed_message, dictionary_size+ORIGINAL_DICTIONARY_SIZE):
            #    uncompressed_message = uncompressed_message+i
                
            #if uncompressed_message == data:
            #    print "TRUE"
                
            #else:
             #   print "NOT TRUE!"
             #   break
        average_compression_ratio = ratio_sum / number_of_datas
        #file_handle.write(''.join([str(compression_ratio)+"," for compression_ratio in compression_ratios])+"\n")
        
        print average_compression_ratio
        #file_handle.write(str(dictionary_size)+","+str(average_compression_ratio)+"\n")
        #file_handle.flush()
            
        #list_of_dictionary_size = list_of_dictionary_size + [compression_ratios]
        
    #file_handle.close()
    return temp 
    #return list_of_dictionary_size
